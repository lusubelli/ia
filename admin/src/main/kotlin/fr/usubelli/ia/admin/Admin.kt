package fr.usubelli.ia.admin

import fr.usubelli.ia.admin.core.Environment
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.KeyStoreOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.JWTAuthHandler
import mu.KotlinLogging
import java.util.HashSet


fun main(args: Array<String>) {

    val logger = KotlinLogging.logger {}

    val port = Environment.intProperty("configuration_port", 8282)

    val vertx = Vertx.vertx()
    val httpServer = vertx
            .createHttpServer(HttpServerOptions())
    val router = Router.router(vertx)

    val allowedHeaders = HashSet<String>()
    //allowedHeaders.add("x-requested-with")
    allowedHeaders.add("Access-Control-Allow-Method")
    allowedHeaders.add("Access-Control-Allow-Origin")
    allowedHeaders.add("Access-Control-Allow-Credentials")
    //allowedHeaders.add("origin")
    allowedHeaders.add("Content-Type")
    //allowedHeaders.add("accept")
    //allowedHeaders.add("X-PINGARUNER")

    val allowedMethods = HashSet<HttpMethod>()
    allowedMethods.add(HttpMethod.GET)
    allowedMethods.add(HttpMethod.POST)
    allowedMethods.add(HttpMethod.OPTIONS)
    allowedMethods.add(HttpMethod.DELETE)
    allowedMethods.add(HttpMethod.PATCH)
    allowedMethods.add(HttpMethod.PUT)


    val config = JWTAuthOptions()
            .setKeyStore(KeyStoreOptions()
                    .setPath("keystore.jceks")
                    .setPassword("secret"))
    // Create a JWT Auth Provider
    val jwt = JWTAuth.create(vertx, config)

    // This cookie handler will be called for all routes
    router.route().handler(CookieHandler.create())
    router.route().handler(CorsHandler.create("http://localhost:4200").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods))
    // protect the API
    // router.route("/rest/1.0/*").handler(JWTAuthHandler.create(jwt, "/rest/1.0/login"))
    router.route("/rest/1.0/*")
            .handler { context ->

                println(context.getCookie("ia-admin-auth"))

                context.next()
            }

    vertx.deployVerticle(AuthVerticle(jwt, router))
    vertx.deployVerticle(ConfigurationVerticle(router))
    vertx.deployVerticle(UserVerticle(router))
    vertx.deployVerticle(NlpVerticle(router))

    httpServer
            .requestHandler(router)
            .listen(port)

    logger.info { "Administration service is started on port $port" }

}