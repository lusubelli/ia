package fr.usubelli.ia

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.WebClient
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val defaultVertxOptions = VertxOptions().apply {
        maxWorkerExecuteTime = 1000 * 60L * 1000 * 1000000
        warningExceptionTime = 1000L * 1000 * 1000000
    }
    val vertx: Vertx by lazy { Vertx.vertx(defaultVertxOptions) }
    vertx.deployVerticle(AudioWebClientVerticle())
}

class AudioWebClientVerticle : AbstractVerticle() {

    override fun start() {
        // The path to the audio file to transcribe
        val fileName = "D:\\workspace\\ia\\src\\main\\resources\\charcuterie.flac"

        // Reads the audio file into memory
        val path = Paths.get(fileName)
        val data = Files.readAllBytes(path)
        val buffer = Buffer.buffer(data)

        val client = WebClient.create(vertx)

// Send a GET request
        client
                .post(8080, "localhost", "/speechtotext")
                .putHeader("content-type", "audio/flac")
                .sendBuffer(buffer, { ar ->
                    if (ar.succeeded()) {
                        // Obtain response
                        val response = ar.result()

                        println("Received response with status code " + response.statusCode())
                        println("Received response with message" )
                        println(response.bodyAsString())
                    } else {
                        println("Something went wrong " + ar.cause().message)
                    }
                })
    }
}
