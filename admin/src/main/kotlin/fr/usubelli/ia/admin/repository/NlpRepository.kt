package fr.usubelli.ia.admin.repository

import java.util.*


interface NlpRepository<T, N> {

    fun inbox(locale: Locale): List<T>

    fun removeClassification(locale: Locale, classificationId: String): T?

    fun classified(locale: Locale): List<N>

    fun insertClassified(locale: Locale, sentence: N): N?

    fun updateClassified(locale: Locale, sentenceId: String, sentence: N): N?

    fun removeClassified(locale: Locale, sentenceId: String): N?

    fun intents(locale: Locale): List<String>

    fun names(locale: Locale): List<String>

}