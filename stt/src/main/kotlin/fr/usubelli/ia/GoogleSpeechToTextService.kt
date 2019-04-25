package fr.usubelli.ia

import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.protobuf.ByteString


class GoogleSpeechToTextService(
        val configuration: RecognitionConfig,
        private val client: SpeechClient = SpeechClient.create()) : SpeechToTextService {

    override fun speechToText(audioBytes: ByteString): String? {
        val audio = RecognitionAudio.newBuilder()
                .setContent(audioBytes)
                .build()

        // Performs speech recognition on the audio file
        val response = client.recognize(configuration, audio)
        val results = response.resultsList

        return results
                .flatMap { result -> result.alternativesList }
                .map { alternative -> alternative.transcript }
                .firstOrNull()

    }

}
