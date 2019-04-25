package fr.usubelli.ia.recognition

import io.reactivex.Observable


data class Detections(val detections: List<Detection>)
data class Detection(val x: Int, val y: Int, val width: Int, val height: Int)

class DetectionService(private val detector: Detector) {

    fun detect(file: String): Observable<Detections> {
        return detector.detect(file)
    }

}
