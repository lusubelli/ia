package fr.usubelli.ia.admin.core

object Environment {

    private fun findProperty(name: String): String? {
        return System.getProperty(name) ?: System.getenv(name)
    }

    fun property(name: String, defaultValue: String): String = findProperty(name)
            ?: defaultValue

    fun intProperty(name: String, defaultValue: Int): Int = findProperty(name)?.toInt() ?: defaultValue

    fun longProperty(name: String, defaultValue: Long): Long = findProperty(name)?.toLong() ?: defaultValue

    fun booleanProperty(name: String, defaultValue: Boolean): Boolean = findProperty(name)?.toBoolean() ?: defaultValue

}