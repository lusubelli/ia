package fr.usubelli.ia.admin.service

import fr.usubelli.ia.admin.repository.cache.CacheConfiguration

interface ConfigurationService {

    fun create(configuration: CacheConfiguration): CacheConfiguration

    fun update(id: String, configuration: CacheConfiguration): CacheConfiguration

    fun load(id: String): CacheConfiguration?

    fun load(): List<CacheConfiguration>

    fun remove(id: String): CacheConfiguration?

}