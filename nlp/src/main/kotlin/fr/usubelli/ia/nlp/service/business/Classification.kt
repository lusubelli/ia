package fr.usubelli.ia.nlp.service.business

data class Classification(
        val id: String,
        val text: String,
        val intentClassification: List<IntentClassification>,
        val nameClassification: List<NameClassification>)

data class IntentClassification(
        val outcome: String,
        val probability: Double)

data class NameClassification(
        val type: String,
        val start: Int,
        val end: Int,
        val probability: Double)