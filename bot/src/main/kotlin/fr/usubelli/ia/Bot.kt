package fr.usubelli.ia

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.usubelli.ia.http.*
import io.reactivex.Observable
import java.awt.Color
import java.awt.Graphics2D
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


class Bot {

    private val retrofitSttClient = RetrofitSttClient()
    private val retrofitNlpClient = RetrofitNlpClient()
    private val retrofitTtsClient = RetrofitTtsClient()
    private val retrofitRecogniseClient = RetrofitRecogniseClient()

    fun say(voice: ByteArray): Observable<ByteArray> {
        return sayMuted(voice)
                .flatMap { answer -> retrofitTtsClient.textToSpeech(answer) }
    }

    fun sayMuted(voice: ByteArray): Observable<String> {
        return retrofitSttClient
                .speechToText(voice)
                .flatMap { text -> retrofitNlpClient.classify(text) }
                .map { classification -> answer(classification) }
    }

    fun write(text: String): Observable<ByteArray> {
        return writeMuted(text)
                .flatMap { answer -> retrofitTtsClient.textToSpeech(answer) }
    }

    fun writeMuted(text: String): Observable<String> {
        return retrofitNlpClient
                .classify(text)
                .map { classification -> answer(classification) }
    }

    fun recognise(image: ByteArray): Observable<ByteArray> {
        return retrofitRecogniseClient
            .recognise(image)
            .map { recognitions ->
                answer(image, recognitions)
            }
    }

    private fun answer(classification: Classification): String {
        println("Classification --> ${jacksonObjectMapper().writeValueAsString(classification)}")

        return "Désolé, je ne comprends pas."
    }

    private fun answer(image: ByteArray, recognitions: Recognitions): ByteArray {
        println("Recognitions --> ${jacksonObjectMapper().writeValueAsString(recognitions)}")

        return extractFaces(image, recognitions)
    }

    private fun extractFaces(image: ByteArray, recognitions: Recognitions): ByteArray {
        val img = ImageIO.read(ByteArrayInputStream(image))
        val graphics = img.graphics as Graphics2D
        graphics.color = Color.RED
        recognitions
                .recognitions
                .map { recognition ->
                    graphics.drawRect(
                            recognition.detection.x,
                            recognition.detection.y,
                            recognition.detection.width,
                            recognition.detection.height)
                    graphics.drawString(
                            recognition.matchings[0].annotation,
                            recognition.detection.x + 10,
                            recognition.detection.y + 20)
                }

        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(img, "png", byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

}
