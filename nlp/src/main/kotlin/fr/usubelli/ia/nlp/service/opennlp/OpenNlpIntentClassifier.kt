package fr.usubelli.ia.nlp.service.opennlp

import fr.usubelli.ia.nlp.service.business.IntentClassification
import opennlp.tools.ml.maxent.GISModel
import opennlp.tools.tokenize.SimpleTokenizer

class OpenNlpIntentClassifier(private val model: GISModel) {

    fun classifyIntent(text: String): List<IntentClassification> {
        return model.eval(SimpleTokenizer.INSTANCE.tokenize(text))
                .mapIndexed { index, d -> index to d }
                .sortedByDescending { it.second }
                .map { outcome ->
                    val index = outcome.first
                    val proba = outcome.second
                    IntentClassification(model.getOutcome(index), proba)
                }
    }

}

