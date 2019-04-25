package fr.usubelli.ia.admin.repository

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

interface UserRepository {

    fun save(user: CacheUser): CacheUser

    fun load(id: String): CacheUser?

    fun load(): List<CacheUser>

    fun remove(id: String): CacheUser?

}


@JsonIgnoreProperties(ignoreUnknown = true)
data class CacheUser(
        val email: String,
        val password: String)