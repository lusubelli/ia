package fr.usubelli.ia.recognition.openimaj

import fr.usubelli.ia.recognition.Recognizer
import org.openimaj.feature.DoubleFVComparison
import org.openimaj.image.FImage
import org.openimaj.image.ImageUtilities
import org.openimaj.image.processing.face.alignment.IdentityAligner
import org.openimaj.image.processing.face.detection.DetectedFace
import org.openimaj.image.processing.face.detection.IdentityFaceDetector
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class OpenImajRecognizer: Recognizer {

    private val engine = FaceRecognitionEngine(
        IdentityFaceDetector(),
        EigenFaceRecogniser.create<DetectedFace, String>(
            100,
            IdentityAligner(),
            1,
            DoubleFVComparison.EUCLIDEAN))

    override fun train(dataSet: Map<String, List<FImage>>) {
        dataSet.keys.forEach { personId ->
            dataSet[personId]?.forEach { personImage ->
                engine.train(personId, personImage)
            }
        }
    }

    override fun recognize(image: BufferedImage): Matching {
        val face = ImageUtilities.createFImage(image)
        val best = engine.recogniseBest(face)[0].secondObject()
        println("Actual: s31\tguess: ${best.annotation}\tconfidence: ${best.confidence}")
        return Matching(best.annotation, best.confidence)
    }

}

fun main(args: Array<String>) {

    val recognizer: Recognizer = OpenImajRecognizer()

    val dataSet = File("D:\\perso\\workspace\\ia\\recognition\\src\\main\\resources\\face-dataset\\")
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

    recognizer.train(dataSet)

    val best = recognizer.recognize(
        ImageIO.read(File("D:\\perso\\workspace\\ia\\recognition\\src\\main\\resources\\face-dataset\\a\\face-recognition-3.jpg")))
    println("Guess: ${best.annotation}\tconfidence: ${best.confidence}")

}

data class Matching(val annotation: String, val confidence: Float)
