package fr.usubelli.ia.admin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.usubelli.ia.admin.repository.CacheUser
import fr.usubelli.ia.admin.repository.UserRepository
import fr.usubelli.ia.admin.repository.cache.CacheUserRepository
import fr.usubelli.ia.admin.service.AuthService
import fr.usubelli.ia.admin.service.AuthServiceImpl
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.jwt.JWTOptions
import io.vertx.ext.web.Cookie
import io.vertx.ext.web.Router
import mu.KotlinLogging

data class Auth(val email: String, val password: String)

class AuthVerticle(
        private val jwt: JWTAuth,
        private val router: Router): AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    override fun start() {

        val userRepository: UserRepository = CacheUserRepository(hashMapOf(
            "admin@gmail.com" to CacheUser("admin@gmail.com", "password")
        ))
        val authService: AuthService = AuthServiceImpl(userRepository)

        router.post("/rest/1.0/login")
                .handler { context ->

                    context.request().bodyHandler { buffer ->

                        val auth = jacksonObjectMapper().readValue<Auth>(buffer.toString(), Auth::class.java)

                        val start = System.currentTimeMillis()

                        logger.debug { "User is logging" }


                        val user = authService.login(auth)

                        val httpResponse = context.response()
                        if (user != null) {
                            val token = jwt.generateToken(JsonObject().put("email", user.email), JWTOptions().setExpiresInSeconds(60))
                            context.addCookie(Cookie.cookie("ia-admin-auth", token))
                            logger.debug { "User logged in in ${System.currentTimeMillis() - start}" }

                            httpResponse.statusCode = 200
                            httpResponse.putHeader("content-type", "application/json")
                            httpResponse.end(jacksonObjectMapper().writeValueAsString(user))
                        } else {
                            httpResponse.statusCode = 404
                            httpResponse.end()
                        }

                    }
                }

    }

}
