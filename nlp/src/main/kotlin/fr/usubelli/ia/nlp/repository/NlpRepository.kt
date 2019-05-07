package fr.usubelli.ia.nlp.repository

import fr.usubelli.ia.nlp.repository.document.ClassificationDocument
import fr.usubelli.ia.nlp.repository.document.ClassifiedSentenceDocument
import java.util.*

interface NlpRepository {

    fun inbox(locale: Locale, classificationDocument: ClassificationDocument)

    fun classified(locale: Locale): List<ClassifiedSentenceDocument>

}