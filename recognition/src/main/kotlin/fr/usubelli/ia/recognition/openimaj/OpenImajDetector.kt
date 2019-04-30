package fr.usubelli.ia.recognition.openimaj

import fr.usubelli.ia.recognition.Detection
import fr.usubelli.ia.recognition.Detections
import fr.usubelli.ia.recognition.Detector
import io.reactivex.Observable
import org.openimaj.image.ImageUtilities
import org.openimaj.image.processing.face.detection.HaarCascadeDetector
import java.io.File


class OpenImajDetector: Detector {

    private val haarCascadeDetector = HaarCascadeDetector()

    override fun detect(file: String): Observable<Detections> {
        return Observable.create<Detections> { emitter ->
            haarCascadeDetector
                    .detectFaces(ImageUtilities.readF(File(file)))
                    .let { detectedFaces ->
                        emitter.onNext(Detections(detectedFaces
                                .toList()
                                .map { detectedFace ->
                                    Detection(
                                            detectedFace.bounds.x.toInt(),
                                            detectedFace.bounds.y.toInt(),
                                            detectedFace.bounds.width.toInt(),
                                            detectedFace.bounds.height.toInt())
                                }))
                        emitter.onComplete()
                    }
        }
    }

}
