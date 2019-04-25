package fr.usubelli.ia.service

import fr.usubelli.ia.service.business.Classification
import io.reactivex.Observable
import java.util.*

interface NlpService {

    fun train(locale: Locale): Observable<Void>

    fun classify(locale: Locale, text: String): Observable<Classification>?

}