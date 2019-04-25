package fr.usubelli.ia.admin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.usubelli.ia.admin.core.Environment
import fr.usubelli.ia.admin.repository.ConfigurationRepository
import fr.usubelli.ia.admin.repository.cache.CacheConfiguration
import fr.usubelli.ia.admin.repository.cache.CacheConfigurationRepository
import fr.usubelli.ia.admin.repository.cache.CacheWifi
import fr.usubelli.ia.admin.service.ConfigurationService
import fr.usubelli.ia.admin.service.ConfigurationServiceImpl
import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import mu.KotlinLogging

class ConfigurationVerticle(private val router: Router) : AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    override fun start() {

        val host = Environment.property("configuration_host", "http://localhost:8282/")

        val configurationRepository: ConfigurationRepository = CacheConfigurationRepository(hashMapOf(
                "1" to CacheConfiguration("1", "Hydrogen", CacheWifi("ssid", "password")),
                "2" to CacheConfiguration("2", "Helium", CacheWifi("ssid", "password")),
                "3" to CacheConfiguration("3", "Lithium", CacheWifi("ssid", "password")),
                "4" to CacheConfiguration("4", "Beryllium", CacheWifi("ssid", "password")),
                "5" to CacheConfiguration("5", "Boron", CacheWifi("ssid", "password")),
                "6" to CacheConfiguration("6", "Carbon", CacheWifi("ssid", "password")),
                "7" to CacheConfiguration("7", "Nitrogen", CacheWifi("ssid", "password")),
                "8" to CacheConfiguration("8", "Oxygen", CacheWifi("ssid", "password")),
                "9" to CacheConfiguration("9", "Fluorine", CacheWifi("ssid", "password")),
                "10" to CacheConfiguration("10", "Neon", CacheWifi("ssid", "password"))
        ))
        val configurationService: ConfigurationService = ConfigurationServiceImpl(host, configurationRepository)

        router.post("/rest/1.0/configurations")
                .handler { context ->

                    context.request().bodyHandler { buffer ->

                        val configuration = jacksonObjectMapper().readValue<CacheConfiguration>(buffer.toString(), CacheConfiguration::class.java)

                        val start = System.currentTimeMillis()

                        logger.debug { "Configuration is creating" }

                        val saved = configurationService.create(configuration)

                        logger.debug { "Configuration created in ${System.currentTimeMillis() - start}" }

                        val httpResponse = context.response()
                        httpResponse.statusCode = 200
                        httpResponse.putHeader("content-type", "application/json")
                        httpResponse.end(jacksonObjectMapper().writeValueAsString(saved))

                    }
                }

        router.put("/rest/1.0/configurations/:id")
                .handler { context ->

                    context.request().bodyHandler { buffer ->

                        val id = context.request().getParam("id")

                        val configuration = jacksonObjectMapper().readValue<CacheConfiguration>(buffer.toString(), CacheConfiguration::class.java)

                        val start = System.currentTimeMillis()

                        logger.debug { "Configuration is updating" }

                        val saved = configurationService.update(id, configuration)

                        logger.debug { "Configuration updated in ${System.currentTimeMillis() - start}" }

                        val httpResponse = context.response()
                        httpResponse.statusCode = 200
                        httpResponse.putHeader("content-type", "application/json")
                        httpResponse.end(jacksonObjectMapper().writeValueAsString(saved))

                    }
                }

        router.get("/rest/1.0/configurations")
                .handler { context ->

                    val start = System.currentTimeMillis()

                    logger.debug { "Loading configurations" }

                    val configurations = configurationService.load()

                    logger.debug { "Configurations loaded in ${System.currentTimeMillis() - start}" }

                    val httpResponse = context.response()
                    httpResponse.statusCode = 200
                    httpResponse.putHeader("content-type", "application/json")
                    httpResponse.end(ObjectMapper().writeValueAsString(configurations))
                }

        router.delete("/rest/1.0/configurations/:id")
                .handler { context ->

                    val start = System.currentTimeMillis()

                    val id = context.request().getParam("id")

                    logger.debug { "Deleting configuration of $id" }

                    val configuration = configurationService.remove(id)

                    logger.debug { "Configuration of $id loaded in ${System.currentTimeMillis() - start}" }

                    val httpResponse = context.response()
                    httpResponse.statusCode = 200
                    httpResponse.end(ObjectMapper().writeValueAsString(configuration))
                }

        router.get("/rest/1.0/configurations/:id")
                .handler { context ->

                    val start = System.currentTimeMillis()

                    val id = context.request().getParam("id")

                    logger.debug { "Loading configuration of $id" }

                    val configuration = configurationService.load(id)

                    logger.debug { "Configuration of $id loaded in ${System.currentTimeMillis() - start}" }

                    val httpResponse = context.response()
                    if (configuration == null) {
                        httpResponse.statusCode = 404
                        httpResponse.end()
                    } else {
                        httpResponse.statusCode = 200
                        httpResponse.putHeader("content-type", "application/json")
                        httpResponse.end(ObjectMapper().writeValueAsString(configuration))
                    }
                }
    }

}