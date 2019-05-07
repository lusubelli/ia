package fr.usubelli.ia.nlp.repository.mongo

import com.mongodb.client.MongoDatabase
import fr.usubelli.ia.nlp.repository.NlpRepository
import fr.usubelli.ia.nlp.repository.document.ClassificationDocument
import fr.usubelli.ia.nlp.repository.document.ClassifiedSentenceDocument
import java.util.*


class MongoNlpRepository(private val database: MongoDatabase): NlpRepository {

    override fun classified(locale: Locale): List<ClassifiedSentenceDocument> {
        return database
                .getCollection<ClassifiedSentenceDocument>("classified_${locale.language}", ClassifiedSentenceDocument::class.java)
                .find()
                .toList()
    }

    override fun inbox(locale: Locale, classificationDocument: ClassificationDocument) {
        database
                .getCollection<ClassificationDocument>("inbox_${locale.language}", ClassificationDocument::class.java)
                .insertOne(classificationDocument)
    }

}