package dev.toastbits.lifelog.application.settings.domain.model

import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.configuration.castType
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceType
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceTypeRegistry
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import kotlinx.serialization.Serializable

@Serializable
data class SerialisedDatabaseSourceConfiguration(
    val typeId: String,
    val serialisedConfiguration: String
)

fun <C: DatabaseSourceConfiguration<T>, T: LogDatabase> DatabaseSourceTypeRegistry.serialiseConfiguration(
    configuration: C
): SerialisedDatabaseSourceConfiguration {
    val type: DatabaseSourceType<C, T> = configuration.castType()
    val typeId: String =
        try {
            getIdOfType(type)
        }
        catch (e: Throwable) {
            throw RuntimeException("Cannot serialise configuration $configuration with type $type because the type is not registered", e)
        }

    return SerialisedDatabaseSourceConfiguration(typeId, type.serialiseConfiguration(configuration))
}

fun DatabaseSourceTypeRegistry.deserialiseConfiguration(
    serialisedConfiguration: SerialisedDatabaseSourceConfiguration
): DatabaseSourceConfiguration<*> {
    val type: DatabaseSourceType<*, *> =
        try {
            getById(serialisedConfiguration.typeId)
        }
        catch (e: Throwable) {
            throw RuntimeException("Cannot deserialise configuration $serialisedConfiguration with type ID '${serialisedConfiguration.typeId}' because the type is not registered", e)
        }

    return type.deserialiseConfiguration(serialisedConfiguration.serialisedConfiguration)
}
