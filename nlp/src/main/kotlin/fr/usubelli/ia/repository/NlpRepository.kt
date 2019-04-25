package fr.usubelli.ia.repository

import fr.usubelli.ia.repository.document.ClassificationDocument
import fr.usubelli.ia.repository.document.ClassifiedSentenceDocument
import java.util.*

interface NlpRepository {

    fun inbox(locale: Locale, classificationDocument: ClassificationDocument)

    fun classified(locale: Locale): List<ClassifiedSentenceDocument>

}