package fr.usubelli.ia

import com.google.protobuf.ByteString

interface SpeechToTextService {

    fun speechToText(audioBytes: ByteString): String?

}