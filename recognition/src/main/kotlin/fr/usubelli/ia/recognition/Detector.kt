package fr.usubelli.ia.recognition

import io.reactivex.Observable

interface Detector {

    fun detect(file: String): Observable<Detections>

}