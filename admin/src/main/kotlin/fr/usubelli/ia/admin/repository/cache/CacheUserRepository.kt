package fr.usubelli.ia.admin.repository.cache

import fr.usubelli.ia.admin.repository.CacheUser
import fr.usubelli.ia.admin.repository.UserRepository
import java.util.*

class CacheUserRepository(private val users: MutableMap<String, CacheUser> = HashMap()): UserRepository {

    override fun save(cacheUser: CacheUser): CacheUser {
        users[cacheUser.email] = cacheUser
        return users[cacheUser.email]!!
    }

    override fun load(id: String): CacheUser? {
        return users[id]
    }

    override fun load(): List<CacheUser> {
        return users.values.toList()
    }

    override fun remove(id: String): CacheUser? {
        return users.remove(id)
    }

}

