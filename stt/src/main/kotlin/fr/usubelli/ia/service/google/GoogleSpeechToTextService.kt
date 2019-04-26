package fr.usubelli.ia.service.google

import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.protobuf.ByteString
import fr.usubelli.ia.service.SpeechToTextService
import io.reactivex.Observable


class GoogleSpeechToTextService(
    private val configuration: RecognitionConfig,
    private val client: SpeechClient = SpeechClient.create()) : SpeechToTextService {

    override fun speechToText(audioBytes: ByteString): Observable<String?> {
        return Observable.create { emitter ->
            val audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build()

            emitter.onNext(
                client
                    .recognize(configuration, audio)
                    .resultsList
                    .flatMap { result -> result.alternativesList }
                    .map { alternative -> alternative.transcript }
                    .first())

            emitter.onComplete()
        }

    }

}
