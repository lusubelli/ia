package fr.usubelli.ia.nlp.service.opennlp

import fr.usubelli.ia.nlp.service.business.NameClassification
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.tokenize.SimpleTokenizer
import opennlp.tools.util.Span

class OpenNlpNameClassifier(private val model: NameFinderME?) {

    fun classifyName(text: String): List<NameClassification> {
        return if(model == null) {
            emptyList()
        } else {
            val tokens = SimpleTokenizer.INSTANCE.tokenize(text)
            val spans = model.find(tokens)
            spans.map { span ->
                NameClassification(
                        span.type,
                        start(tokens, span),
                        end(tokens, span),
                        span.prob)
            }.toList()
        }
    }

    private fun start(tokens: Array<String>, span: Span): Int {
        return tokens.take(span.start).map { token -> token.length + 1 }.sum()
    }

    private fun end(tokens: Array<String>, span: Span): Int {
        return tokens.take(span.end).map { token -> token.length + 1 }.sum() -1
    }

}