package fr.usubelli.ia.recognition

import fr.usubelli.ia.recognition.openimaj.Matching
import org.openimaj.image.FImage
import java.awt.image.BufferedImage

interface Recognizer {

    fun train(dataSet: Map<String, List<FImage>>)

    fun recognize(image: BufferedImage): Matching

}