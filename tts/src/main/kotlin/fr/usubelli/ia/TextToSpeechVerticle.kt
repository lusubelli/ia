package fr.usubelli.ia

import fr.usubelli.ia.service.GoogleTextToSpeechService
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpHeaders
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.RoutingContext


class TextToSpeechVerticle(private val googleTextToSpeechService: GoogleTextToSpeechService) : AbstractVerticle() {

    override fun start() {

        val router = Router.router(vertx)

        router
            .get("/texttospeech")
            .handler { rc -> textToSpeech(rc) }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080)
    }

    private fun textToSpeech(rc: RoutingContext) {
        googleTextToSpeechService
            .textToSpeech(rc.request().getParam("query"))
            .subscribe(
                { path ->
                    rc.response()
                        .setChunked(true)
                        .setStatusCode(201)
                        .putHeader("content-type", "audio/mp3")
                        .putHeader("Content-Disposition", "attachment; filename=\"$path\"")
                        .putHeader(HttpHeaders.TRANSFER_ENCODING, "chunked")
                        .sendFile(path)
                },
                { error ->
                    rc.response()
                        .setStatusCode(500)
                        .end(error.message)
                },
                {})
    }

}

fun main(args: Array<String>) {
    Vertx
        .vertx(VertxOptions())
        .deployVerticle(TextToSpeechVerticle(GoogleTextToSpeechService()))
}

