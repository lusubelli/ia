package fr.usubelli.ia.nlp.repository.file

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.usubelli.ia.nlp.repository.NlpRepository
import fr.usubelli.ia.nlp.repository.document.ClassificationDocument
import fr.usubelli.ia.nlp.repository.document.ClassifiedSentenceDocument
import java.io.File
import java.util.*

class FileNlpRepository(private val rootDirectoryPath: String) : NlpRepository {

    private val objectMapper = jacksonObjectMapper()

    override fun classified(locale: Locale): List<ClassifiedSentenceDocument> {
        return objectMapper
            .readValue(File("$rootDirectoryPath\\${locale.language}\\classified.json").readText(), object : TypeReference<List<ClassifiedSentenceDocument>>() {})
    }

    override fun inbox(locale: Locale, classificationDocument: ClassificationDocument) {
        val inbox: MutableMap<String?, ClassificationDocument> = objectMapper
            .readValue<List<ClassificationDocument>>(File("$rootDirectoryPath\\${locale.language}\\inbox.json").readText(), object : TypeReference<List<ClassificationDocument>>() {})
            .map { sentence -> sentence._id to sentence }
            .toMap()
            .toMutableMap()

        inbox[classificationDocument._id] = classificationDocument

        objectMapper
            .writeValue(File("$rootDirectoryPath\\${locale.language}\\inbox.json"), inbox.values)

    }

}