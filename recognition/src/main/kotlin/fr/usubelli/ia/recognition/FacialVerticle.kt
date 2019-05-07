package fr.usubelli.ia.recognition

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.usubelli.ia.recognition.service.RecogniserService
import fr.usubelli.ia.recognition.service.business.Detection
import fr.usubelli.ia.recognition.service.business.Detections
import fr.usubelli.ia.recognition.service.opencv.OpenCVRecogniser
import fr.usubelli.ia.recognition.service.openimaj.OpenImajRecogniser
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.RoutingContext
import io.vertx.reactivex.ext.web.handler.CorsHandler
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO


class FacialVerticle(
        private val recogniserService: RecogniserService) : AbstractVerticle() {

    private val objectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun start() {
        val router = Router.router(vertx)
        router.route().handler(corsHandler())

        router.post("/recognise")
                .handler { rc -> recognise(rc) }

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8383)
    }

    private fun recognise(rc: RoutingContext) {
        rc.request().bodyHandler { buffer ->
            recogniserService
            .detect(buffer.bytes)
            .flatMap { detections ->
                val source = ByteArrayInputStream(buffer.bytes).readBytes()
                recogniserService.recognise(
                        source,
                        extractFace(source, detections))
            }.subscribe({ recognitions ->
                rc.response()
                        .setChunked(true)
                        .setStatusCode(201)
                        .putHeader("content-type", "application/json")
                        .putHeader(HttpHeaders.TRANSFER_ENCODING, "chunked")
                        .end(objectMapper.writeValueAsString(recognitions))
            }, { error ->
                rc.response()
                        .setStatusCode(400)
                        .end(error.message.toString())
                error.printStackTrace()
            }, {

            })
        }
    }

    private fun extractFace(source: ByteArray, detections: Detections): Map<Detection, ByteArray> {
        return detections
                .detections
                .map { detection ->
                    val b = ByteArrayOutputStream()
                    ImageIO.write(ImageIO.read(ByteArrayInputStream(source)).getSubimage(
                            detection.x + (detection.width - 92),
                            detection.y + (detection.height - 100),
                            92,
                            112), "jpg", b)
                    detection to b.toByteArray()
                }
                .toMap()
    }

    private fun corsHandler(): CorsHandler {

        val allowedHeaders = HashSet<String>()
        allowedHeaders.add("x-requested-with")
        allowedHeaders.add("Access-Control-Allow-Origin")
        allowedHeaders.add("origin")
        allowedHeaders.add("Content-Type")
        allowedHeaders.add("accept")
        allowedHeaders.add("X-PINGARUNER")

        return CorsHandler
                .create("*")
                .allowedHeaders(allowedHeaders)
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.PATCH)
                .allowedMethod(HttpMethod.PUT)
    }

}

fun main(args: Array<String>) {
    val api = (System.getProperty("api") ?: System.getenv("api")) ?: "openimaj"
    val recogniser = if (api == "opencv") {
        OpenCVRecogniser("D:\\workspace\\ia\\recognition\\src\\main\\resources\\lbpcascade_frontalface.xml")
    } else {
        OpenImajRecogniser()
    }

    recogniser
            .train(File("D:\\workspace\\ia\\recognition\\src\\main\\resources\\face-dataset\\")
                    .listFiles()
                    .map { personDirectory ->
                        val personId = personDirectory.name
                        val personImages = personDirectory
                                .listFiles()
                                .map { faceFile -> FileInputStream(faceFile).readBytes() }
                        personId to personImages
                    }.toMap())
            .subscribe({
                println("never print")
            }, { error ->
                error.printStackTrace()
            }, {
                println("model loaded")
                Vertx
                        .vertx(VertxOptions())
                        .deployVerticle(FacialVerticle(
                                recogniser))
            })

}