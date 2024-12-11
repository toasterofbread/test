package dev.toastbits.lifelog.application.settings.domain.group

import dev.toastbits.composekit.settings.ComposeKitSettingsGroup
import dev.toastbits.composekit.settings.PlatformSettingsProperty
import dev.toastbits.kogit.core.model.GitCredentials
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceTypeRegistry
import dev.toastbits.lifelog.application.settings.domain.model.SerialisedDatabaseSourceConfiguration

@Suppress("PropertyName")
interface DatabaseSourcePreferencesGroup: ComposeKitSettingsGroup {
    val sourceTypeRegistry: DatabaseSourceTypeRegistry

    val AUTO_OPEN_SOURCE_INDEX: PlatformSettingsProperty<Int>
    val DATABASE_SOURCES: PlatformSettingsProperty<List<SerialisedDatabaseSourceConfiguration>>

    val GIT_USERNAME: PlatformSettingsProperty<String>
    val GIT_PASSWORD: PlatformSettingsProperty<String>
}

suspend fun DatabaseSourcePreferencesGroup.getGitCredentials(): GitCredentials? {
    val gitUsername: String = GIT_USERNAME.get()
    val gitPassword: String = GIT_PASSWORD.get()

    return (
        if (gitUsername.isNotBlank() || gitPassword.isNotBlank()) GitCredentials(gitUsername, gitPassword)
        else null
    )
}
