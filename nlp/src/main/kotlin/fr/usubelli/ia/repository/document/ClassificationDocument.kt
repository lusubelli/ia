package fr.usubelli.ia.repository.document

data class ClassificationDocument(
        val _id: String,
        val text: String,
        val intentClassification: List<IntentClassificationDocument>,
        val nameClassification: List<NameClassificationDocument>)

data class IntentClassificationDocument(
        val outcome: String,
        val probability: Double)

data class NameClassificationDocument(
        val type: String,
        val start: Int,
        val end: Int,
        val probability: Double)