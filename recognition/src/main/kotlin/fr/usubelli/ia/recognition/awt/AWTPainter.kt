package fr.usubelli.ia.recognition.awt

import fr.usubelli.ia.recognition.Detections
import java.awt.Color
import java.awt.Graphics2D
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class AWTPainter {

    fun paintDetections(file: String, detections: Detections): String {
        val image = ImageIO.read(File(file))
        val graphics = image.graphics as Graphics2D
        graphics.color = Color.RED
        detections
                .detections
                .forEach { recognition ->
                    graphics.drawRect(
                            recognition.x,
                            recognition.y,
                            recognition.width,
                            recognition.height)
                }
        val path = "D:\\tmp\\${UUID.randomUUID()}"
        ImageIO.write(image, "png", File(path))
        return path
    }

}
