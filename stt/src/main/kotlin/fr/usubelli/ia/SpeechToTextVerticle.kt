package fr.usubelli.ia

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.protobuf.ByteString
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.ext.web.Router

class SpeechToTextVerticle : AbstractVerticle() {

    override fun start() {

        // Builds the sync recognize request
        val config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.FLAC)
                .setSampleRateHertz(48000)
                .setLanguageCode("fr-FR")
                .build()

        val router = Router.router(vertx)
        val googleSpeechToTextService = GoogleSpeechToTextService(config)
        val objectMapper = ObjectMapper()

        router.post("/speechtotext").handler({ rc ->

            rc.request().bodyHandler{ buffer ->
                Thread(Runnable {
                    kotlin.run {
                        val text = googleSpeechToTextService.speechToText(ByteString.copyFrom(buffer.bytes))
                        rc.response().end(objectMapper.writeValueAsString(Response(text)))
                    }
                }).start()
            }

        })

        vertx.createHttpServer()
                .requestHandler({ router.accept(it) })
                .listen(8080)
    }


}

data class Response(val text: String?)

fun main(args: Array<String>) {
    val defaultVertxOptions = VertxOptions().apply {
        maxWorkerExecuteTime = 1000 * 60L * 1000 * 1000000
        warningExceptionTime = 1000L * 1000 * 1000000
    }
    val vertx: Vertx by lazy { Vertx.vertx(defaultVertxOptions) }
    vertx.deployVerticle(SpeechToTextVerticle())
}

