package dev.toastbits.lifelog.application.settings.data.group

import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceType
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceTypeRegistry

internal class MapDatabaseSourceTypeRegistry(
    private val map: Map<String, DatabaseSourceType<*, *>>
): DatabaseSourceTypeRegistry {
    override fun getAll(): Map<String, DatabaseSourceType<*, *>> = map

    override fun getById(id: String): DatabaseSourceType<*, *> =
        map[id] ?: throw RuntimeException("Source type with id '$id' not found in registry")

    override fun getIdOfType(type: DatabaseSourceType<*, *>): String =
        map.entries.firstOrNull { it.value::class == type::class }?.key
            ?: throw RuntimeException("Source type with class ${type::class} not found in registry")
}
