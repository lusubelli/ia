package fr.usubelli.ia.admin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.usubelli.ia.admin.repository.mongo.MongoClassified
import fr.usubelli.ia.admin.repository.mongo.MongoName
import fr.usubelli.ia.admin.repository.mongo.MongoNlpRepository
import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.PermittedOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import mu.KotlinLogging
import org.litote.kmongo.KMongo
import java.util.*


class NlpVerticle(private val router: Router): AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    override fun start() {

        /*
        val nlpRepository = CacheNlpRepository(
                "D:\\workspace\\ia\\database")
        */
        val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
        val database = client.getDatabase("nlp")
        val nlpRepository = MongoNlpRepository(database)

        router.get("/rest/1.0/inbox/:locale")
                .handler { context ->

                    logger.debug { "Classifications are loading" }

                    val locale = Locale.forLanguageTag(context.request().getParam("locale"))

                    val classifications = nlpRepository.inbox(locale)
                            .map { mongoClassification -> mongoClassification.toClassification() }

                    logger.debug { "Classifications are loaded" }

                    val httpResponse = context.response()
                    httpResponse.statusCode = 200
                    httpResponse.putHeader("content-type", "application/json")
                    httpResponse.end(jacksonObjectMapper().writeValueAsString(classifications))

                }

        router.post("/rest/1.0/classified/:locale")
                .handler { context ->

                    context.request().bodyHandler { buffer ->

                        logger.debug { "Sentences is updating" }

                        val locale = Locale.forLanguageTag(context.request().getParam("locale"))

                        val classified = jacksonObjectMapper().readValue<Classified>(buffer.toString(), Classified::class.java)

                        val created = nlpRepository.insertClassified(locale, classified.toMongoClassified())

                        logger.debug { "Sentences was updated" }

                        val httpResponse = context.response()
                        if (created == null) {
                            httpResponse.statusCode = 404
                            httpResponse.end()
                        } else {
                            httpResponse.statusCode = 200
                            httpResponse.putHeader("content-type", "application/json")
                            httpResponse.end(jacksonObjectMapper().writeValueAsString(created.toClassified()))
                            vertx.eventBus().send("classified-created", jacksonObjectMapper().writeValueAsString(created.toClassified()))
                        }

                    }

                }

        router.get("/rest/1.0/classified/:locale")
                .handler { context ->

                    logger.debug { "Sentences are loading" }

                    val locale = Locale.forLanguageTag(context.request().getParam("locale"))

                    val sentences = nlpRepository.classified(locale).map { mongoClassified -> mongoClassified.toClassified() }

                    logger.debug { "Sentences are loaded" }

                    val httpResponse = context.response()
                    httpResponse.statusCode = 200
                    httpResponse.putHeader("content-type", "application/json")
                    httpResponse.end(jacksonObjectMapper().writeValueAsString(sentences))

                }

        router.put("/rest/1.0/classified/:locale/:id")
                .handler { context ->

                    context.request().bodyHandler { buffer ->

                        logger.debug { "Sentences is updating" }

                        val locale = Locale.forLanguageTag(context.request().getParam("locale"))

                        val id = context.request().getParam("id")

                        val sentence = jacksonObjectMapper().readValue<Classified>(buffer.toString(), Classified::class.java)

                        val updated = nlpRepository.updateClassified(locale, id, sentence.toMongoClassified())

                        logger.debug { "Sentences was updated" }

                        val httpResponse = context.response()
                        if (updated == null) {
                            httpResponse.statusCode = 404
                            httpResponse.end()
                        } else {
                            httpResponse.statusCode = 200
                            httpResponse.putHeader("content-type", "application/json")
                            httpResponse.end(jacksonObjectMapper().writeValueAsString(updated.toClassified()))
                            vertx.eventBus().send("classified-updated", jacksonObjectMapper().writeValueAsString(updated.toClassified()))
                        }

                    }

                }

        router.delete("/rest/1.0/classified/:locale/:id")
                .handler { context ->

                    logger.debug { "Sentence is deleting" }

                    val locale = Locale.forLanguageTag(context.request().getParam("locale"))

                    val id = context.request().getParam("id")

                    val removed = nlpRepository.removeClassified(locale, id)

                    logger.debug { "Sentence was deleted" }

                    val httpResponse = context.response()
                    if (removed == null) {
                        httpResponse.statusCode = 404
                        httpResponse.end()
                    } else {
                        httpResponse.statusCode = 200
                        httpResponse.putHeader("content-type", "application/json")
                        httpResponse.end(jacksonObjectMapper().writeValueAsString(removed.toClassified()))
                        vertx.eventBus().send("classified-removed", jacksonObjectMapper().writeValueAsString(removed.toClassified()))
                    }

                }


        router.delete("/rest/1.0/inbox/:locale/:id")
                .handler { context ->

                    logger.debug { "Classification is deleting" }

                    val locale = Locale.forLanguageTag(context.request().getParam("locale"))

                    val id = context.request().getParam("id")

                    val removed = nlpRepository.removeClassification(locale, id)

                    logger.debug { "Classification was deleted" }

                    val httpResponse = context.response()
                    if (removed == null) {
                        httpResponse.statusCode = 404
                        httpResponse.end()
                    } else {
                        httpResponse.statusCode = 200
                        httpResponse.putHeader("content-type", "application/json")
                        httpResponse.end(jacksonObjectMapper().writeValueAsString(removed.toClassification()))
                        vertx.eventBus().send("classification-removed", jacksonObjectMapper().writeValueAsString(removed.toClassification()))
                    }

                }

        router.get("/rest/1.0/intents/:locale")
                .handler { context ->

                    logger.debug { "Intents are loading" }

                    val locale = Locale.forLanguageTag(context.request().getParam("locale"))

                    val intents = nlpRepository.intents(locale)

                    logger.debug { "Intents are loaded" }

                    val httpResponse = context.response()
                    httpResponse.statusCode = 200
                    httpResponse.putHeader("content-type", "application/json")
                    httpResponse.end(jacksonObjectMapper().writeValueAsString(intents))

                }


        router.get("/rest/1.0/names/:locale")
                .handler { context ->

                    logger.debug { "Names are loading" }

                    val locale = Locale.forLanguageTag(context.request().getParam("locale"))

                    val names = nlpRepository.names(locale)

                    logger.debug { "Names are loaded" }

                    val httpResponse = context.response()
                    httpResponse.statusCode = 200
                    httpResponse.putHeader("content-type", "application/json")
                    httpResponse.end(jacksonObjectMapper().writeValueAsString(names))

                }

        val options = BridgeOptions()
                .addOutboundPermitted(PermittedOptions().setAddress("classification-removed"))
                .addOutboundPermitted(PermittedOptions().setAddress("classified-created"))
                .addOutboundPermitted(PermittedOptions().setAddress("classified-updated"))
                .addOutboundPermitted(PermittedOptions().setAddress("classified-removed"))
        router.route("/rest/1.0/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options))

        nlpRepository.toto(Locale.FRENCH)

    }

}


data class Classified(
        val id: String,
        val text: String,
        val intent: String,
        val names: List<Name> = emptyList()) {

    fun toMongoClassified(): MongoClassified {
        return MongoClassified(
                id,
                text,
                intent,
                names.map { name -> name.toMongName() }
        )
    }

}

data class Name(val name: String,
         val role: String,
         val start: Int,
         val end: Int) {

    fun toMongName(): MongoName {
        return MongoName(
                name,
                role,
                start,
                end)
    }

}

data class Classification(
        val id: String,
        val text: String,
        val intentClassification: List<IntentClassification>,
        val nameClassification: List<NameClassification>)

data class IntentClassification(
        val outcome:String,
        val probability:Double)

data class NameClassification(
        val type: String,
        val start: Int,
        val end: Int,
        val probability: Double)