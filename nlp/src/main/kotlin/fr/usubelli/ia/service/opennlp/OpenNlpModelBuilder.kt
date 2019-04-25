package fr.usubelli.ia.service.opennlp

import fr.usubelli.ia.service.business.ClassifiedSentence
import fr.usubelli.ia.service.business.ClassifiedName
import opennlp.tools.ml.maxent.GISModel
import opennlp.tools.ml.maxent.GISTrainer
import opennlp.tools.ml.model.AbstractDataIndexer.CUTOFF_PARAM
import opennlp.tools.ml.model.Event
import opennlp.tools.ml.model.OnePassRealValueDataIndexer
import opennlp.tools.ml.model.TwoPassDataIndexer
import opennlp.tools.namefind.BilouCodec
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.NameSample
import opennlp.tools.namefind.TokenNameFinderFactory
import opennlp.tools.tokenize.SimpleTokenizer
import opennlp.tools.util.ObjectStreamUtils
import opennlp.tools.util.Span
import opennlp.tools.util.TrainingParameters

private const val MIN_BUILD_SIZE = 2

class OpenNlpModelBuilder {

    fun buildIntentModel(expressions: List<ClassifiedSentence>): GISModel {

        return if (expressions.size < MIN_BUILD_SIZE) {
            GISModel(arrayOf(), arrayOf(), arrayOf())
        } else {
            val events = ObjectStreamUtils.createObjectStream(expressions
                    .map { expression ->
                        Event(expression.intent, SimpleTokenizer.INSTANCE.tokenize(expression.text))
                    })
            val dataIndexer = if (expressions.size < 100) {
                OnePassRealValueDataIndexer()
            } else {
                TwoPassDataIndexer()
            }
            val param = TrainingParameters()
            if (expressions.size < 1000) {
                param.put(CUTOFF_PARAM, 1)
            }
            /*
            NlpApplicationConfiguration.intentConfiguration.properties.forEach {
                param.put(it.key.toString(), it.value?.toString())
            }
            */
            dataIndexer.init(param, null)
            dataIndexer.index(events)
            GISTrainer().trainModel(1000, dataIndexer)
        }

    }

    fun buildNameModel(expressions: List<ClassifiedSentence>, language: String): NameFinderME? {

        val trainingEvents = if (expressions.size >= MIN_BUILD_SIZE) {
            expressions
                .map { expression ->
                    NameSample(
                            SimpleTokenizer.INSTANCE.tokenize(expression.text),
                            buildSpans(expression.text, expression.names).toTypedArray().sortedArray(),
                            false)
                }
        } else emptyList()
        /*
        NlpApplicationConfiguration.entityConfiguration.properties.forEach {
            params.put(it.key.toString(), it.value?.toString())
        }
        */
        return if (trainingEvents.size < 5) {
            null
        } else {
            NameFinderME(NameFinderME.train(
                    language,
                    null,
                    ObjectStreamUtils.createObjectStream(trainingEvents),
                    TrainingParameters(),
                    TokenNameFinderFactory(null, null, BilouCodec())
            ))
        }
    }

    private fun buildSpans(text: String, names: List<ClassifiedName>): List<Span> {
        val tokenizer = SimpleTokenizer.INSTANCE
        val tokens = tokenizer.tokenize(text)
        return names.mapNotNull { name ->
            val start = if (name.start == 0) 0 else tokenizer.tokenize(text.substring(0, name.start)).size
            val end = start + tokenizer.tokenize(text.substring(name.start, name.end)).size
            if (start >= tokens.size || end > tokens.size) {
                null
            } else {
                Span(start, end, name.role)
            }
        }.toList()
    }

}