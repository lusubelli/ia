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
import javax.imageio.ImageIO


class OpenImajDetector(private val eigen: EigenImages) : Detector {

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


    private lateinit var features: MutableMap<String, Array<DoubleFV?>>

    fun train(nTraining: Int) {
        val dataset = VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER)

        val nTesting = 5
        val splits = GroupedRandomSplitter<String, FImage>(dataset, nTraining, 0, nTesting)

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


        eigen.train(DatasetAdaptors.asList(splits.trainingDataset))
        features = mutableMapOf()
        val groups = splits.trainingDataset.groups
        for (person in groups) {
            val fvs = arrayOfNulls<DoubleFV>(nTraining)

            for (i in 0 until nTraining) {
                val person = splits.trainingDataset[person]
                fvs[i] = eigen.extractFeature(person?.get(i))
            }
            features[person] = fvs
        }
    }

    fun findBestPerson(image: FImage) : Matching? {
        val testFeature = eigen.extractFeature(image)

        var bestPerson: String? = null
        var minDistance = Double.MAX_VALUE
        for (person in features.keys) {
            for (fv in features[person]!!) {
                val distance = fv!!.compare(testFeature, DoubleFVComparison.EUCLIDEAN)

                if (distance < minDistance) {
                    minDistance = distance
                    bestPerson = person
                }
            }
        }

        return if (bestPerson == null)
            null
        else
            Matching(bestPerson, minDistance)

    }

}

fun main(args: Array<String>) {
    val nTraining = 5
    /*val eigen = EigenImages(100)
    eigen.train(DatasetAdaptors.asList(splits.trainingDataset))
    val features = features(nTraining, eigen, splits.trainingDataset)*/

    /*
    var i = 0
    for (g in 0 until splits.testDataset.groups.size) {
        val group = splits.testDataset.groups.toList()[g]
        val listDataset = splits.testDataset[group]
        for (f in 0 until (listDataset!!.size -1)) {
            val face = listDataset[f]
            val bufferedImage = ImageUtilities.createBufferedImage(face)
            ImageIO.write(bufferedImage, "jpg", File("D:\\workspace\\ia\\recognition\\src\\main\\resources\\face-dataset\\face-recognition-$i.jpg"))
            i++
        }
    }
*/
    val openImajDetector = OpenImajDetector(EigenImages(100))
    openImajDetector.train(nTraining)
    val matching = openImajDetector.findBestPerson(
            ImageUtilities.createFImage(
                    ImageIO.read(
                            File("D:\\workspace\\ia\\recognition\\src\\main\\resources\\face-dataset\\h\\face-recognition-31.jpg"))))
    /*
    val eigenFaces = ArrayList<FImage>()
    for (i in 0..11) {
        eigenFaces.add(eigen.visualisePC(i))
    }
    DisplayUtilities.display("EigenFaces", eigenFaces)
*/

    if (matching != null) {
        println("Guess: ${matching.name}\tdistance: ${matching.distance}")
    }


}

data class Matching(val name: String, val distance: Double)