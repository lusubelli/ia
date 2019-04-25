package fr.usubelli.ia

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.videoio.VideoCapture
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte


class TakePicture {

    fun run() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        val capture = VideoCapture(0)
        val matrix = Mat()
        capture.read(matrix)
        if( capture.isOpened && capture.read(matrix)) {
            val image = BufferedImage(matrix.width(),
                    matrix.height(), BufferedImage.TYPE_3BYTE_BGR)

            val raster = image.raster
            val dataBuffer = raster.dataBuffer as DataBufferByte
            val data = dataBuffer.data
            matrix.get(0, 0, data)
            Imgcodecs.imwrite("D:\\workspace\\ia\\recognition\\src\\main\\resources\\sanpshot.jpg", matrix)
        }
    }

}

fun main(args: Array<String>) {
    TakePicture().run()
}