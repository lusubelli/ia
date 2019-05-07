package fr.usubelli.ia.recognition.service.business

data class Recognitions(val recognitions: List<Recognition>)

data class Recognition(val detection: Detection, val matchings: List<Matching>)