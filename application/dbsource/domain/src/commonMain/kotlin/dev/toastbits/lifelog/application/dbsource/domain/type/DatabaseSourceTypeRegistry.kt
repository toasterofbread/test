package dev.toastbits.lifelog.application.dbsource.domain.type

interface DatabaseSourceTypeRegistry {
    fun getAll(): Map<String, DatabaseSourceType<*, *>>
    fun getById(id: String): DatabaseSourceType<*, *>
    fun getIdOfType(type: DatabaseSourceType<*, *>): String
}
