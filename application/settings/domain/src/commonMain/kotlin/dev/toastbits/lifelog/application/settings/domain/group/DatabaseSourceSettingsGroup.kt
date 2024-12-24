package dev.toastbits.lifelog.application.settings.domain.group

import dev.toastbits.composekit.settings.ComposeKitSettingsGroup
import dev.toastbits.composekit.settingsitem.domain.PlatformSettingsProperty
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceTypeRegistry
import dev.toastbits.lifelog.application.settings.domain.model.SerialisedDatabaseSourceConfiguration

@Suppress("PropertyName")
interface DatabaseSourceSettingsGroup: ComposeKitSettingsGroup {
    val sourceTypeRegistry: DatabaseSourceTypeRegistry

    val AUTO_OPEN_SOURCE_INDEX: PlatformSettingsProperty<Int>
    val DATABASE_SOURCES: PlatformSettingsProperty<List<SerialisedDatabaseSourceConfiguration>>

    val GIT_BINARY_PATH: PlatformSettingsProperty<String>

    val USE_SYSTEM_GIT_CREDENTIALS: PlatformSettingsProperty<Boolean>
    val GIT_USERNAME: PlatformSettingsProperty<String>
    val GIT_PASSWORD: PlatformSettingsProperty<String>
}
