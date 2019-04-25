package fr.usubelli.ia.recognition.openimaj

import fr.usubelli.ia.recognition.Detection
import fr.usubelli.ia.recognition.Detections
import fr.usubelli.ia.recognition.Detector
import io.reactivex.Observable
import org.openimaj.data.dataset.VFSGroupDataset
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter
import org.openimaj.experiment.dataset.util.DatasetAdaptors
import org.openimaj.feature.DoubleFV
import org.openimaj.feature.DoubleFVComparison
import org.openimaj.image.FImage
import org.openimaj.image.ImageUtilities
import org.openimaj.image.model.EigenImages
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

fun main(args: Array<String>) {
    val dataset = VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER)

    val nTraining = 5
    val nTesting = 5
    val splits = GroupedRandomSplitter<String, FImage>(dataset, nTraining, 0, nTesting)

    val eigen = EigenImages(100)
    eigen.train(DatasetAdaptors.asList(splits.trainingDataset))

    val features = mutableMapOf<String, Array<DoubleFV?>>()
    for (person in splits.trainingDataset.groups) {
        val fvs = arrayOfNulls<DoubleFV>(nTraining)

        for (i in 0 until nTraining) {
            val face = splits.trainingDataset[person]?.get(i)
            fvs[i] = eigen.extractFeature(face)
        }
        features[person] = fvs
    }

    for (truePerson in splits.testDataset.groups) {
        for (face in splits.testDataset[truePerson]!!) {
            val testFeature = eigen.extractFeature(face)

            var bestPerson: String? = null
            var minDistance = java.lang.Double.MAX_VALUE
            for (person in features.keys) {
                for (fv in features[person]!!) {
                    val distance = fv!!.compare(testFeature, DoubleFVComparison.EUCLIDEAN)

                    if (distance < minDistance) {
                        minDistance = distance
                        bestPerson = person
                    }
                }
            }

            println("Actual: $truePerson\tguess: $bestPerson\tdistance: $minDistance")

        }
    }


}