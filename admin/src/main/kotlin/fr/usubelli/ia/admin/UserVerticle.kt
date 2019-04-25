package fr.usubelli.ia.admin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.usubelli.ia.admin.repository.CacheUser
import fr.usubelli.ia.admin.repository.UserRepository
import fr.usubelli.ia.admin.repository.cache.CacheUserRepository
import fr.usubelli.ia.admin.service.UserService
import fr.usubelli.ia.admin.service.UserServiceImpl
import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import mu.KotlinLogging

class UserVerticle(private val router: Router): AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    override fun start() {

        val userRepository: UserRepository = CacheUserRepository(hashMapOf(
            "admin@gmail.com" to CacheUser("admin@gmail.com", "password")
        ))
        val userService: UserService = UserServiceImpl(userRepository)

        router.post("/rest/1.0/users")
                .handler { context ->

                    context.request().bodyHandler { buffer ->

                        val user = jacksonObjectMapper().readValue<CacheUser>(buffer.toString(), CacheUser::class.java)

                        val start = System.currentTimeMillis()

                        logger.debug { "User is creating" }

                        val saved = userService.create(user)

                        logger.debug { "User created in ${System.currentTimeMillis() - start}" }

                        val httpResponse = context.response()
                        httpResponse.statusCode = 200
                        httpResponse.putHeader("content-type", "application/json")
                        httpResponse.end(jacksonObjectMapper().writeValueAsString(saved))

                    }
                }

        router.put("/rest/1.0/users/:email")
                .handler { context ->

                    context.request().bodyHandler { buffer ->

                        val email = context.request().getParam("email")

                        val user = jacksonObjectMapper().readValue<CacheUser>(buffer.toString(), CacheUser::class.java)

                        val start = System.currentTimeMillis()

                        logger.debug { "User is updating" }

                        val saved = userService.update(email, user)

                        logger.debug { "User updated in ${System.currentTimeMillis() - start}" }

                        val httpResponse = context.response()
                        httpResponse.statusCode = 200
                        httpResponse.putHeader("content-type", "application/json")
                        httpResponse.end(jacksonObjectMapper().writeValueAsString(saved))

                    }
                }

        router.get("/rest/1.0/users")
                .handler { context ->

                    val start = System.currentTimeMillis()

                    logger.debug { "Loading users" }

                    val users = userService.load()

                    logger.debug { "Users loaded in ${System.currentTimeMillis() - start}" }

                    val httpResponse = context.response()
                    httpResponse.statusCode = 200
                    httpResponse.putHeader("content-type", "application/json")
                    httpResponse.end(ObjectMapper().writeValueAsString(users))
                }

        router.delete("/rest/1.0/users/:email")
                .handler { context ->

                    val start = System.currentTimeMillis()

                    val email = context.request().getParam("email")

                    logger.debug { "Deleting user of $email" }

                    val user = userService.remove(email)

                    logger.debug { "User of $email loaded in ${System.currentTimeMillis() - start}" }

                    val httpResponse = context.response()
                    httpResponse.statusCode = 200
                    httpResponse.end(ObjectMapper().writeValueAsString(user))
                }

        router.get("/rest/1.0/users/:email")
                .handler { context ->

                    val start = System.currentTimeMillis()

                    val email = context.request().getParam("email")

                    logger.debug { "Loading user of $email" }

                    val user = userService.load(email)

                    logger.debug { "User of $email loaded in ${System.currentTimeMillis() - start}" }

                    val httpResponse = context.response()
                    if (user == null) {
                        httpResponse.statusCode = 404
                        httpResponse.end()
                    } else {
                        httpResponse.statusCode = 200
                        httpResponse.putHeader("content-type", "application/json")
                        httpResponse.end(ObjectMapper().writeValueAsString(user))
                    }
                }

    }

}
