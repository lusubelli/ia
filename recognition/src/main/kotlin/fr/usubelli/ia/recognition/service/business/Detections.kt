package fr.usubelli.ia.recognition.service.business

data class Detections(val detections: List<Detection>)

data class Detection(val x: Int, val y: Int, val width: Int, val height: Int)