package fr.usubelli.ia.nlp.repository.document

data class ClassifiedSentenceDocument(
        val _id: String,
        val text: String,
        val intent: String,
        val names: List<ClassifiedNameDocument> = emptyList())

data class ClassifiedNameDocument(val name: String,
                                  val role: String,
                                  val start: Int,
                                  val end: Int)