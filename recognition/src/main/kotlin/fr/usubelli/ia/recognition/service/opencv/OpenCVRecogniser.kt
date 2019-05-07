package fr.usubelli.ia.recognition.service.opencv

import fr.usubelli.ia.recognition.service.RecogniserService
import fr.usubelli.ia.recognition.service.business.*
import io.reactivex.Observable
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.DoublePointer
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.MatVector
import org.bytedeco.opencv.opencv_core.RectVector
import org.bytedeco.opencv.opencv_face.EigenFaceRecognizer
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier
import java.nio.ByteBuffer

class OpenCVRecogniser(classifier: String) : RecogniserService {

    //private val faceRecognizer = FisherFaceRecognizer.create()
    private val faceRecognizer = EigenFaceRecognizer.create()
    //private val faceRecognizer = LBPHFaceRecognizer.create()
    private val cascadeClassifier = CascadeClassifier(classifier)

    var labels: Map<Int, String> = mapOf()

    override fun train(dataSet: Map<String, List<ByteArray>>): Observable<Void> {
        labels = labels(dataSet)
        return Observable.create {
            val ids = ids(dataSet)
            faceRecognizer.train(images(dataSet), ids)
            it.onComplete()
        }
    }

    override fun detect(image: ByteArray): Observable<Detections> {
        return Observable.create<Detections> { emitter ->
            val faceDetections = RectVector()
            val testImage = opencv_imgcodecs.imdecode(Mat(BytePointer(ByteBuffer.wrap(image)), false), opencv_imgcodecs.IMREAD_GRAYSCALE)
            cascadeClassifier.detectMultiScale(testImage, faceDetections)
            emitter.onNext(Detections(faceDetections
                    .get()
                    .map { detection ->
                        Detection(
                                detection.x(),
                                detection.y(),
                                detection.width(),
                                detection.height())
                    }))
            emitter.onComplete()
        }
    }

    override fun recognise(source: ByteArray, faces: Map<Detection, ByteArray>): Observable<Recognitions> {
        return Observable.create {
            it.onNext(Recognitions(faces.keys.map { detection ->
                val face = faces[detection]
                val testImage = opencv_imgcodecs.imdecode(Mat(BytePointer(ByteBuffer.wrap(face)), false), opencv_imgcodecs.IMREAD_GRAYSCALE)
                val label = IntPointer(1)
                val confidence = DoublePointer(1)
                faceRecognizer.predict(testImage, label, confidence)
                val predictedLabel = labels[label.get(0)]!!
                val predictedConfidence = confidence.get(0)
                Recognition(detection, listOf(Matching(predictedLabel, predictedConfidence.toFloat())))
            }))
            it.onComplete()
        }
    }


    private fun labels(dataSet: Map<String, List<ByteArray>>): Map<Int, String> {
        var i = 1
        return dataSet
                .keys
                .map { personUuid -> i++ to personUuid }
                .toMap()
    }

    private fun ids(dataSet: Map<String, List<ByteArray>>): Mat {
        return Mat(IntPointer(*dataSet
                .keys
                .flatMap { uuid ->
                    val id = id(uuid)
                    dataSet[uuid]!!.map { id }
                }.toIntArray()))
    }

    private fun id(uuid: String): Int {
        labels
                .entries
                .forEach { (personId, personUuid) ->
                    if (personUuid == uuid) {
                        return personId
                    }
                }
        return -1
    }

    private fun images(dataSet: Map<String, List<ByteArray>>): MatVector {
        return MatVector(*dataSet
                .keys
                .flatMap { uuid ->
                    dataSet[uuid]!!.map { image ->
                        opencv_imgcodecs.imdecode(Mat(BytePointer(ByteBuffer.wrap(image)), false), 0)
                    }
                }.toTypedArray())
    }

}

