package fr.usubelli.ia.service

import com.google.protobuf.ByteString
import io.reactivex.Observable

interface SpeechToTextService {

    fun speechToText(audioBytes: ByteString): Observable<String?>

}