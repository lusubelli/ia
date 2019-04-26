package fr.usubelli.ia.service

import com.google.cloud.texttospeech.v1.AudioConfig
import com.google.cloud.texttospeech.v1.AudioEncoding
import com.google.cloud.texttospeech.v1.SsmlVoiceGender
import com.google.cloud.texttospeech.v1.SynthesisInput
import com.google.cloud.texttospeech.v1.TextToSpeechClient
import com.google.cloud.texttospeech.v1.VoiceSelectionParams
import io.reactivex.Observable
import java.io.File
import java.io.FileOutputStream

class GoogleTextToSpeechService {

    fun textToSpeech(text: String?): Observable<String> {
        return Observable.create<String> { emitter ->
            val textToSpeechClient = TextToSpeechClient.create()

            val input = SynthesisInput.newBuilder()
                .setText(text)
                .build()
            val voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("en-US")
                .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                .build()
            val audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build()
            val response = textToSpeechClient.synthesizeSpeech(
                input, voice,
                audioConfig
            )
            val audioContents = response.audioContent
            val file = File("output.mp3")
            FileOutputStream(file).use { out ->
                out.write(audioContents.toByteArray())
                println("Audio content written to file \"output.mp3\"")
            }

            emitter.onNext(file.absolutePath)

            emitter.onComplete()
        }
    }

}