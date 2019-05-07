package fr.usubelli.ia.nlp.service

import fr.usubelli.ia.nlp.service.business.Classification
import io.reactivex.Observable
import java.util.*

interface NlpService {

    fun train(locale: Locale): Observable<Void>

    fun classify(locale: Locale, text: String): Observable<Classification>?

}