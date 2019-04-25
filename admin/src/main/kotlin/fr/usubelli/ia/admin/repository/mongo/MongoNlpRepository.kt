package fr.usubelli.ia.admin.repository.mongo

import com.mongodb.Block
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import fr.usubelli.ia.admin.*
import fr.usubelli.ia.admin.repository.NlpRepository
import java.util.*
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.changestream.FullDocument


class MongoNlpRepository(private val database: MongoDatabase): NlpRepository<MongoClassification, MongoClassified> {

    override fun inbox(locale: Locale): List<MongoClassification> {
        return database
                .getCollection<MongoClassification>("inbox_${locale.language}", MongoClassification::class.java)
                .find()
                .toList()
    }

    override fun removeClassification(locale: Locale, classificationId: String): MongoClassification? {
        return database
                .getCollection<MongoClassification>("inbox_${locale.language}", MongoClassification::class.java)
                .findOneAndDelete(Filters.eq("_id", classificationId))
    }

    override fun classified(locale: Locale): List<MongoClassified> {
        return database
                .getCollection<MongoClassified>("classified_${locale.language}", MongoClassified::class.java)
                .find()
                .toList()
    }

    override fun insertClassified(locale: Locale, classified: MongoClassified): MongoClassified? {
        database
                .getCollection<MongoClassified>("classified_${locale.language}", MongoClassified::class.java)
                .insertOne(classified)
        return classified
    }

    override fun updateClassified(locale: Locale, classifiedId: String, classified: MongoClassified): MongoClassified? {
        database
                .getCollection<MongoClassified>("classified_${locale.language}", MongoClassified::class.java)
                .updateOne(
                        Filters.eq("_id", classifiedId),
                        Updates.set("_id", classified._id))
        return classified
    }

    override fun removeClassified(locale: Locale, sentenceId: String): MongoClassified? {
        return database
                .getCollection<MongoClassified>("classified_${locale.language}", MongoClassified::class.java)
                .findOneAndDelete(Filters.eq("_id", sentenceId))
    }

    override fun intents(locale: Locale): List<String> {
        return classified(locale).map { classified -> classified.intent }.distinct()
    }

    override fun names(locale: Locale): List<String> {
        return classified(locale).flatMap { classified -> classified.names }.map { name -> name.role }.distinct()
    }

    fun toto(locale: Locale){
/*
        database
                .getCollection<MongoClassification>("inbox_${locale.language}", MongoClassification::class.java)
                .watch(listOf(Aggregates.match(Filters.`in`("operationType", listOf("insert", "update", "replace", "delete")))))
                .fullDocument(FullDocument.UPDATE_LOOKUP).forEach(Block { changeStreamDocument -> println(" MyService:::" + changeStreamDocument.fullDocument) })
                */
    }

}


data class MongoClassified(
        val _id: String,
        val text: String,
        val intent: String,
        val names: List<MongoName> = emptyList()) {

    fun toClassified(): Classified {
        return Classified(
                _id,
                text,
                intent,
                names.map { name -> name.toName() }
        )
    }

}

data class MongoName(val name: String,
                     val role: String,
                     val start: Int,
                     val end: Int) {

    fun toName(): Name {
        return Name(
                name,
                role,
                start,
                end)
    }

}


data class MongoClassification(
        val _id: String,
        val text: String,
        val intentClassification: List<MongoIntentClassification>,
        val nameClassification: List<MongoNameClassification>) {

    fun toClassification(): Classification {
        return Classification(
                _id,
                text,
                intentClassification.map { mongoIntentClassification -> mongoIntentClassification.toIntentClassification() },
                nameClassification.map { mongoNameClassification -> mongoNameClassification.toNameClassification() }
        )
    }

}

data class MongoIntentClassification(
        val outcome:String,
        val probability:Double) {

    fun toIntentClassification(): IntentClassification {
        return IntentClassification(
                outcome,
                probability)
    }

}

data class MongoNameClassification(
        val type: String,
        val start: Int,
        val end: Int,
        val probability: Double) {

    fun toNameClassification(): NameClassification {
        return NameClassification(
                type,
                start,
                end,
                probability)
    }

}