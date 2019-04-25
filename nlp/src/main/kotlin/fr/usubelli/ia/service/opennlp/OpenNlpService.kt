package fr.usubelli.ia.service.opennlp

import fr.usubelli.ia.repository.NlpRepository
import fr.usubelli.ia.repository.document.ClassificationDocument
import fr.usubelli.ia.repository.document.IntentClassificationDocument
import fr.usubelli.ia.repository.document.NameClassificationDocument
import fr.usubelli.ia.service.NlpService
import fr.usubelli.ia.service.business.Classification
import fr.usubelli.ia.service.business.ClassifiedSentence
import fr.usubelli.ia.service.business.ClassifiedName
import io.reactivex.Observable
import java.util.*


class OpenNlpService(private val nlpRepository: NlpRepository): NlpService {

    private var openNlpIntentClassifier: OpenNlpIntentClassifier? = null
    private var openNlpEntityClassifier: OpenNlpNameClassifier? = null

    override fun train(locale: Locale): Observable<Void> {
        return Observable.create<Void> {
            val expressions = nlpRepository
                    .classified(locale)
                    .map { c -> ClassifiedSentence(c._id, c.text, c.intent, c.names.map { n -> ClassifiedName(n.name, n.role, n.start, n.end) }) }
            openNlpIntentClassifier = OpenNlpIntentClassifier(
                    OpenNlpModelBuilder().buildIntentModel(expressions))
            openNlpEntityClassifier = OpenNlpNameClassifier(
                    OpenNlpModelBuilder().buildNameModel(expressions, locale.toString()))
            it.onComplete()
        }
    }

    override fun classify(locale: Locale, text: String): Observable<Classification> {
        return Observable.create<Classification> {
            val intentClassification = openNlpIntentClassifier?.classifyIntent(text) ?: listOf()
            val nameClassification = openNlpEntityClassifier?.classifyName(text) ?: listOf()
            val classification = Classification(UUID.randomUUID().toString(), text, intentClassification, nameClassification)
            it.onNext(classification)
            nlpRepository.inbox(Locale.FRENCH, ClassificationDocument(
                    classification.id,
                    classification.text,
                    classification.intentClassification.map { ic -> IntentClassificationDocument(ic.outcome, ic.probability) },
                    classification.nameClassification.map { nc -> NameClassificationDocument(nc.type, nc.start, nc.end, nc.probability) }))
            it.onComplete()
        }
    }

}