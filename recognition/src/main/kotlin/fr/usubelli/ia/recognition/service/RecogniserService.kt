package fr.usubelli.ia.recognition.service

import fr.usubelli.ia.recognition.service.business.Detection
import fr.usubelli.ia.recognition.service.business.Detections
import fr.usubelli.ia.recognition.service.business.Recognitions
import io.reactivex.Observable


interface RecogniserService {

    fun train(dataSet: Map<String, List<ByteArray>>): Observable<Void>

    fun detect(image: ByteArray): Observable<Detections>

    fun recognise(source: ByteArray, faces: Map<Detection, ByteArray>): Observable<Recognitions>

}
