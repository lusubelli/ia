package fr.usubelli.ia

import org.junit.jupiter.api.Test

class OpenCVTest {
    @Test
    fun someOpenCVTest() {
        System.out.printf("java.library.path: %s%n",
                System.getProperty("java.library.path"))
        System.loadLibrary("opencv_java320")
    }
}