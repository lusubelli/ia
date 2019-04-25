package fr.usubelli.ia

import opennlp.tools.chunker.ChunkerME
import opennlp.tools.chunker.ChunkerModel
import opennlp.tools.cmdline.PerformanceMonitor
import opennlp.tools.cmdline.SystemInputStreamFactory
import opennlp.tools.cmdline.postag.POSModelLoader
import opennlp.tools.postag.POSSample
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel
import opennlp.tools.tokenize.WhitespaceTokenizer
import opennlp.tools.util.PlainTextByLineStream
import java.io.File
import java.io.FileInputStream
import opennlp.tools.parser.Parse
import opennlp.tools.cmdline.parser.ParserTool
import opennlp.tools.parser.ParserFactory
import opennlp.tools.parser.ParserModel
import java.io.IOException




class SentenceDetect {

    fun sentenceDetector() {

        val paragraph = "Hi. How are you? This is Mike."

        // always start with a model, a model is learned from training data
        val fileInputStream = FileInputStream("en-sent.bin")
        val model = SentenceModel(fileInputStream)
        val sdetector = SentenceDetectorME(model)

        val sentences = sdetector.sentDetect(paragraph)

        println(sentences[0])
        println(sentences[1])
        fileInputStream.close()
    }

    fun POSTag() {
        val model = POSModelLoader()
                .load(File("en-pos-maxent.bin"))
        val perfMon = PerformanceMonitor(System.err, "sent")
        val tagger = POSTaggerME(model)

        val input = "Hi. How are you? This is Mike."
        val lineStream = PlainTextByLineStream(
                SystemInputStreamFactory(), "UTF-8")

        perfMon.start()
        var line: String
        while (true) {

            line = lineStream.read()
            if (line == null) {
                break
            }
            val whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
                    .tokenize(line)
            val tags = tagger.tag(whitespaceTokenizerLine)

            val sample = POSSample(whitespaceTokenizerLine, tags)
            println(sample.toString())

            perfMon.incrementCounter()
        }
        perfMon.stopAndPrintFinalResult()
    }

    fun chunk() {
        val model = POSModelLoader()
                .load(File("en-pos-maxent.bin"))
        val perfMon = PerformanceMonitor(System.err, "sent")
        val tagger = POSTaggerME(model)

        val input = "Hi. How are you? This is Mike."
        val lineStream = PlainTextByLineStream(
                SystemInputStreamFactory(), "UTF-8")

        perfMon.start()
        var line: String
        var whitespaceTokenizerLine: Array<String>? = null

        var tags: Array<String>? = null
        while (true) {
            line = lineStream.read()
            if (line == null) {
                break
            }

            whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
                    .tokenize(line)
            tags = tagger.tag(whitespaceTokenizerLine)

            val sample = POSSample(whitespaceTokenizerLine!!, tags!!)
            println(sample.toString())
            perfMon.incrementCounter()
        }
        perfMon.stopAndPrintFinalResult()

        // chunker
        val `is` = FileInputStream("en-chunker.bin")
        val cModel = ChunkerModel(`is`)

        val chunkerME = ChunkerME(cModel)
        val result = chunkerME.chunk(whitespaceTokenizerLine!!, tags)

        for (s in result)
            println(s)

        val span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags!!)
        for (s in span)
            System.out.println(s.toString())
    }

    fun Parse() {
        // http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Parser#Training_Tool
        val `is` = FileInputStream("en-parser-chunking.bin")

        val model = ParserModel(`is`)

        val parser = ParserFactory.create(model)

        val sentence = "Programcreek is a very huge and useful website."
        val topParses = ParserTool.parseLine(sentence, parser, 1)

        for (p in topParses)
            p.show()

        `is`.close()

        /*
	 * (TOP (S (NP (NN Programcreek) ) (VP (VBZ is) (NP (DT a) (ADJP (RB
	 * very) (JJ huge) (CC and) (JJ useful) ) ) ) (. website.) ) )
	 */
    }
}

fun main(args: Array<String>) {

}