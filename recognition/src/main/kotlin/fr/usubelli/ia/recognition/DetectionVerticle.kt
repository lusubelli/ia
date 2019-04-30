package fr.usubelli.ia.recognition

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.usubelli.ia.recognition.awt.AWTPainter
import fr.usubelli.ia.recognition.opencv.OpenCVDetector
import fr.usubelli.ia.recognition.openimaj.OpenImajDetector
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.RoutingContext
import io.vertx.reactivex.ext.web.handler.BodyHandler
import io.vertx.reactivex.ext.web.handler.CorsHandler


class DetectionVerticle(private val detectionService: DetectionService) : AbstractVerticle() {

    override fun start() {
        val router = Router.router(vertx)
        router.route().handler(corsHandler())

        router.post("/detection")
                .handler(BodyHandler.create()
                        .setUploadsDirectory("D:\\tmp\\"))
                .handler { rc -> detection(rc) }
        router.post("/detection/png")
                .handler(BodyHandler.create()
                        .setUploadsDirectory("D:\\tmp\\"))
                .handler { rc -> detectionPng(rc) }

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8787)
    }

    private fun detectionPng(rc: RoutingContext) {
        val file = rc.fileUploads().first().uploadedFileName()
        detectionService
                .detect(file)
                .subscribe({ detections ->
                    val path = AWTPainter().paintDetections(file, detections)
                    rc.response()
                            .setChunked(true)
                            .setStatusCode(201)
                            .putHeader("content-type", "image/png")
                            .putHeader("Content-Disposition", "attachment; filename=\"$path\"")
                            .putHeader(HttpHeaders.TRANSFER_ENCODING, "chunked")
                            .sendFile(path)
                }, { error ->
                    rc.response()
                            .setStatusCode(400)
                            .end(error.message.toString())
                }, {

                })
    }

    private fun detection(rc: RoutingContext) {
        val objectMapper = jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        rc.response().putHeader("content-type", "application/json")
        rc.response().end(objectMapper.writeValueAsString(detectionService.detect(rc.fileUploads().first().uploadedFileName())))
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
    val detector = if(api == "opencv") {
        OpenCVDetector("D:\\workspace\\ia\\recognition\\src\\main\\resources\\lbpcascade_frontalface.xml")
    } else {
        OpenImajDetector()
    }

    val detectionService = DetectionService(detector)
    Vertx
            .vertx(VertxOptions())
            .deployVerticle(DetectionVerticle(detectionService))
}