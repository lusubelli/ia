package fr.usubelli.ia

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.protobuf.ByteString
import fr.usubelli.ia.service.google.GoogleSpeechToTextService
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpMethod
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.RoutingContext
import io.vertx.reactivex.ext.web.handler.CorsHandler

class SpeechToTextVerticle(private val googleSpeechToTextService: GoogleSpeechToTextService) : AbstractVerticle() {

    override fun start() {

        val router = Router.router(vertx)
        router.route().handler(corsHandler())

        router
            .post("/speechtotext")
            .handler { rc -> speechToText(rc) }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080)
    }

    private fun speechToText(rc: RoutingContext) {
        rc.request().bodyHandler { buffer ->

            googleSpeechToTextService
                .speechToText(ByteString.copyFrom(buffer.bytes))
                .subscribe(
                    { text ->
                        rc.response()
                            .putHeader("content-type", "application/json")
                            .end(jacksonObjectMapper().writeValueAsString(Response(text)))
                    },
                    { error -> error.printStackTrace() },
                    { println("complete") })

        }
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

data class Response(val text: String?)

fun main(args: Array<String>) {

    // Builds the sync recognize request
    val config = RecognitionConfig.newBuilder()
        .setEncoding(RecognitionConfig.AudioEncoding.FLAC)
        .setSampleRateHertz(48000)
        .setLanguageCode("fr-FR")
        .build()

    Vertx
        .vertx(VertxOptions())
        .deployVerticle(SpeechToTextVerticle(GoogleSpeechToTextService(config)))
}

