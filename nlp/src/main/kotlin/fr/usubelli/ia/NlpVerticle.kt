package fr.usubelli.ia

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.usubelli.ia.repository.file.FileNlpRepository
import fr.usubelli.ia.repository.mongo.MongoNlpRepository
import fr.usubelli.ia.service.opennlp.OpenNlpService
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.reactivex.ext.web.handler.CorsHandler
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.RoutingContext
import org.litote.kmongo.KMongo
import java.util.*
import kotlin.collections.HashSet


class NlpVerticle(private val nlpService: OpenNlpService) : AbstractVerticle() {

    override fun start() {
        val router = Router.router(vertx)
        router.route().handler(corsHandler())

        router.get("/nlp/classify")
                .handler { rc -> classification(rc) }

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
    }

    private fun classification(rc: RoutingContext) {
        val objectMapper = jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        nlpService
                .classify(Locale.FRENCH, rc.request().getParam("query"))
                .subscribe({ classification ->
                    rc.response()
                            .setChunked(true)
                            .setStatusCode(201)
                            .putHeader("content-type", "application/json")
                            .putHeader(HttpHeaders.TRANSFER_ENCODING, "chunked")
                            .end(objectMapper.writeValueAsString(classification))
                }, { error ->
                    rc.response()
                            .setStatusCode(400)
                            .end(error.message.toString())
                }, {

                })

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

    val repository = (System.getProperty("repository") ?: System.getenv("repository")) ?: "file"
    val nlpRepository = if (repository == "mongo") {
        val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
        val database = client.getDatabase("nlp")
        MongoNlpRepository(database)
    } else {
        FileNlpRepository(NlpVerticle::class.java.getResource("/database").file)
    }

    val api = (System.getProperty("api") ?: System.getenv("api")) ?: "opennlp"
    val classifier = if (api == "opennlp") {
        OpenNlpService(nlpRepository)
    } else {
        OpenNlpService(nlpRepository)
    }

    classifier.train(Locale.FRENCH).subscribe({
        println("never print")
    }, { error ->
        error.printStackTrace()
    }, {
        println("model loaded")
        Vertx
                .vertx(VertxOptions())
                .deployVerticle(NlpVerticle(classifier))
    })


}

