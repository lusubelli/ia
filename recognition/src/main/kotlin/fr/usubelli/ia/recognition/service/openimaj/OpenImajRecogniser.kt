package fr.usubelli.ia.recognition.service.openimaj

import fr.usubelli.ia.recognition.service.RecogniserService
import fr.usubelli.ia.recognition.service.business.*
import io.reactivex.Observable
import org.openimaj.feature.DoubleFVComparison
import org.openimaj.image.ImageUtilities
import org.openimaj.image.processing.face.alignment.IdentityAligner
import org.openimaj.image.processing.face.detection.DetectedFace
import org.openimaj.image.processing.face.detection.HaarCascadeDetector
import org.openimaj.image.processing.face.detection.IdentityFaceDetector
import org.openimaj.image.processing.face.recognition.EigenFaceRecogniser
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO


class OpenImajRecogniser: RecogniserService {

    private val haarCascadeDetector = HaarCascadeDetector()
    private val engine = FaceRecognitionEngine(
            IdentityFaceDetector(),
            EigenFaceRecogniser.create<DetectedFace, String>(
                    100,
                    IdentityAligner(),
                    1,
                    DoubleFVComparison.EUCLIDEAN))
    override fun detect(image: ByteArray): Observable<Detections> {
        return Observable.create<Detections> { emitter ->
            haarCascadeDetector
                    .detectFaces(ImageUtilities.createFImage(ImageIO.read(ByteArrayInputStream(image))))
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

    override fun train(dataSet: Map<String, List<ByteArray>>): Observable<Void> {
        return Observable.create { emitter ->
            dataSet.keys.forEach { personId ->
                dataSet[personId]?.forEach { personImage ->
                    engine.train(personId, ImageUtilities.createFImage(ImageIO.read(ByteArrayInputStream(personImage))))
                }
            }
            emitter.onComplete()
        }
    }

    override fun recognise(source: ByteArray, faces: Map<Detection, ByteArray>): Observable<Recognitions> {
        return Observable.create { emitter ->
            emitter.onNext(Recognitions(faces.keys.map { detection ->
                val face = faces[detection]
                Recognition(detection, engine.recogniseBest(ImageUtilities.createFImage(ImageIO.read(ByteArrayInputStream(face))))
                        .map { recognition -> recognition.secondObject }
                        .map { recognitionScore ->
                            Matching(
                                    recognitionScore.annotation,
                                    recognitionScore.confidence)
                        })
            }))

            emitter.onComplete()
        }
    }

}
