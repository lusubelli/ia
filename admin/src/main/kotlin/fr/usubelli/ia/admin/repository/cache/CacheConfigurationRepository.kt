package fr.usubelli.ia.admin.repository.cache

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import fr.usubelli.ia.admin.repository.ConfigurationRepository
import java.util.*

class CacheConfigurationRepository(
        private val configurations: MutableMap<String, CacheConfiguration> = HashMap()) : ConfigurationRepository {

    override fun save(cacheConfiguration: CacheConfiguration): CacheConfiguration {
        val id = if (configurations.keys.any { id -> id == cacheConfiguration.id }) {
            configurations[cacheConfiguration.id] = cacheConfiguration
            cacheConfiguration.id
        } else {
            val id = UUID.randomUUID().toString()
            configurations[id] = CacheConfiguration(id, cacheConfiguration.name, cacheConfiguration.wifi, cacheConfiguration.qr)
            id
        }
        return configurations[id]!!
    }

    override fun load(id: String): CacheConfiguration? {
        return configurations[id]
    }

    override fun load(): List<CacheConfiguration> {
        return configurations.values.toList()
    }

    override fun remove(id: String): CacheConfiguration? {
        return configurations.remove(id)
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CacheConfiguration(
    val id: String,
    val name: String,
    val wifi: CacheWifi,
    val qr: CacheQr? = null)

data class CacheWifi(
    val ssid: String,
    val wep: String)

data class CacheQr(
    val data: String,
    val width: Int,
    val height: Int)