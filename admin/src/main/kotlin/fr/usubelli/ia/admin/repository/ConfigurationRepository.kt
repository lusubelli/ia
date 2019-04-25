package fr.usubelli.ia.admin.repository

import fr.usubelli.ia.admin.repository.cache.CacheConfiguration

interface ConfigurationRepository {

    fun save(cacheConfiguration: CacheConfiguration): CacheConfiguration

    fun load(id: String): CacheConfiguration?

    fun load(): List<CacheConfiguration>

    fun remove(id: String): CacheConfiguration?

}