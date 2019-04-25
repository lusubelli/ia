package fr.usubelli.ia.service.business

data class ClassifiedSentence(val id: String?,
                              val text: String,
                              val intent: String,
                              val names: List<ClassifiedName> = emptyList())

data class ClassifiedName(val name: String,
                          val role: String,
                          val start: Int,
                          val end: Int)