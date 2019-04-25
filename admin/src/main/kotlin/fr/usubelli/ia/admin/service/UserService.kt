package fr.usubelli.ia.admin.service

import fr.usubelli.ia.admin.repository.CacheUser

interface UserService {

    fun create(user: CacheUser): CacheUser

    fun update(email: String, user: CacheUser): CacheUser

    fun load(email: String): CacheUser?

    fun load(): List<CacheUser>

    fun remove(email: String): CacheUser?

}
