package fr.usubelli.ia.admin.repository.cache

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.usubelli.ia.admin.repository.NlpRepository
import java.io.File
import java.util.*

class CacheNlpRepository(
        private val rootDirectoryPath: String) : NlpRepository<CacheClassification, CacheClassified> {

    override fun inbox(locale: Locale): List<CacheClassification> {
        return jacksonObjectMapper()
                .readValue(File("$rootDirectoryPath\\${locale.language}\\inbox.json").readText(), object : TypeReference<List<CacheClassification>>() {})
    }

    override fun classified(locale: Locale): List<CacheClassified> {
        return jacksonObjectMapper()
                .readValue(File("$rootDirectoryPath\\${locale.language}\\classified.json").readText(), object : TypeReference<List<CacheClassified>>() {})
    }

    override fun removeClassification(locale: Locale, classificationId: String): CacheClassification? {
        val inbox: MutableMap<String, CacheClassification> = inbox(locale)
                .map { classification -> classification.id to classification }
                .toMap()
                .toMutableMap()

        val removed = if (inbox.containsKey(classificationId)) {
            inbox.remove(classificationId)
        } else null

        jacksonObjectMapper()
                .writeValue(File("$rootDirectoryPath\\${locale.language}\\inbox.json"), inbox.values)

        return removed
    }

    override fun insertClassified(locale: Locale, sentence: CacheClassified): CacheClassified? {
        val classified: MutableMap<String, CacheClassified> = classified(locale)
                .map { classified -> classified.id to classified }
                .toMap()
                .toMutableMap()

        classified[sentence.id] = sentence

        jacksonObjectMapper()
                .writeValue(File("$rootDirectoryPath\\${locale.language}\\classified.json"), classified.values)

        return sentence

    }

    override fun updateClassified(locale: Locale, sentenceId: String, sentence: CacheClassified): CacheClassified? {
        val sentences: MutableMap<String, CacheClassified> = classified(locale)
                .map { classified -> classified.id to classified }
                .toMap()
                .toMutableMap()

        if (sentences.containsKey(sentenceId)) {
            sentences[sentenceId] = sentence
        }

        jacksonObjectMapper()
                .writeValue(File("$rootDirectoryPath\\${locale.language}\\classified.json"), sentences.values)

        return sentences[sentenceId]!!
    }

    override fun removeClassified(locale: Locale, sentenceId: String): CacheClassified? {
        val classified: MutableMap<String, CacheClassified> = classified(locale)
                .map { classified -> classified.id to classified }
                .toMap()
                .toMutableMap()

        val removed = if (classified.containsKey(sentenceId)) {
            classified.remove(sentenceId)
        } else null

        jacksonObjectMapper()
                .writeValue(File("$rootDirectoryPath\\${locale.language}\\classified.json"), classified.values)

        return removed
    }

    override fun intents(locale: Locale): List<String> {
        return jacksonObjectMapper()
                .readValue<List<CacheClassified>>(File("$rootDirectoryPath\\${locale.language}\\classified.json").readText(), object : TypeReference<List<CacheClassified>>() {})
                .map { classified -> classified.intent }
                .distinct()
    }

    override fun names(locale: Locale): List<String> {
        return jacksonObjectMapper()
                .readValue<List<CacheClassified>>(File("$rootDirectoryPath\\${locale.language}\\classified.json").readText(), object : TypeReference<List<CacheClassified>>() {})
                .flatMap { classified -> classified.names }
                .map { name -> name.name }
                .distinct()
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CacheClassified(
        val id: String,
        val text: String,
        val intent: String,
        val names: List<CacheName> = emptyList())

data class CacheName(val name: String,
                     val role: String,
                     val start: Int,
                     val end: Int)


data class CacheClassification(
        val id: String,
        val text: String,
        val intentClassification: List<CacheIntentClassification>,
        val nameClassification: List<CacheNameClassification>)

data class CacheIntentClassification(
        val outcome:String,
        val probability:Double)

data class CacheNameClassification(
        val type: String,
        val start: Int,
        val end: Int,
        val probability: Double)