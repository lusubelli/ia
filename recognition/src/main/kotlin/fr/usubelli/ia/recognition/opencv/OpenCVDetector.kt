package fr.usubelli.ia.recognition.opencv

import fr.usubelli.ia.recognition.Detector
import fr.usubelli.ia.recognition.Detection
import fr.usubelli.ia.recognition.Detections
import io.reactivex.Observable
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.objdetect.CascadeClassifier


class OpenCVDetector(classifier: String) : Detector {

    private val cascadeClassifier: CascadeClassifier

    init {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        this.cascadeClassifier = CascadeClassifier(classifier)
    }

    override fun detect(file: String): Observable<Detections> {
        return detect(mat(file))
    }

    private fun detect(image: Mat): Observable<Detections> {
        return Observable.create<Detections> { emitter ->
            val faceDetections = MatOfRect()
            cascadeClassifier.detectMultiScale(image, faceDetections)
            emitter.onNext(Detections(faceDetections
                    .toArray()
                    .map { detection ->
                        Detection(
                                detection.x,
                                detection.y,
                                detection.width,
                                detection.height)
                    }))
            emitter.onComplete()
        }
    }

    private fun mat(file: String): Mat {
        return Imgcodecs.imread(file)
    }

}