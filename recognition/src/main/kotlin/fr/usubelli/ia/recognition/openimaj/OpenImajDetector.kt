package fr.usubelli.ia.recognition.openimaj

import fr.usubelli.ia.recognition.Detection
import fr.usubelli.ia.recognition.Detections
import fr.usubelli.ia.recognition.Detector
import io.reactivex.Observable
import org.openimaj.data.dataset.VFSGroupDataset
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter
import org.openimaj.experiment.dataset.util.DatasetAdaptors
import org.openimaj.feature.DoubleFVComparison
import org.openimaj.image.ImageUtilities
import org.openimaj.image.processing.face.alignment.IdentityAligner
import org.openimaj.image.processing.face.detection.DetectedFace
import org.openimaj.image.processing.face.detection.HaarCascadeDetector
import org.openimaj.image.processing.face.detection.IdentityFaceDetector
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine
import java.io.File
import javax.imageio.ImageIO


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

fun main(args: Array<String>) {

    println("Loading dataset")
    val dataSet = File("D:\\workspace\\ia\\recognition\\src\\main\\resources\\face-dataset\\")
            .listFiles()
            .map { personDirectory ->
                val personId = personDirectory.name
                val personImages = personDirectory
                        .listFiles()
                        .map { faceFile ->
                            ImageUtilities.createFImage(
                                    ImageIO.read(faceFile))
                        }
                personId to personImages
            }.toMap()

    println("Loading engine")
    val engine = FaceRecognitionEngine(
            IdentityFaceDetector(),
            EigenFaceRecogniser.create<DetectedFace, String>(
                    100,
                    IdentityAligner(),
                    1,
                    DoubleFVComparison.EUCLIDEAN))

    println("Training engine")
    dataSet.keys.forEach { personId ->
        dataSet[personId]?.forEach { personImage ->
            engine.train(personId, personImage)
        }
    }

    println("Finding best person :")
    val face = ImageUtilities.createFImage(
            ImageIO.read(
                    File("D:\\workspace\\ia\\recognition\\src\\main\\resources\\face-dataset\\a\\face-recognition-3.jpg")))
    val best = engine.recogniseBest(face)[0].secondObject()
    println("Actual: s31\tguess: ${best.annotation}\tconfidence: ${best.confidence}")

}
data class Matching(val name: String, val distance: Double)