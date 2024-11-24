package dev.toastbits.lifelog.application.settings.domain.appsettings

import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.composekit.settings.PlatformSettingsGroup
import dev.toastbits.composekit.commonsettings.impl.ComposeKitSettings
import dev.toastbits.lifelog.application.settings.domain.group.DatabasePreferencesGroup
import dev.toastbits.lifelog.application.settings.domain.group.DatabaseSourcePreferencesGroup
import dev.toastbits.lifelog.application.settings.domain.group.DisplayPreferencesGroup
import dev.toastbits.lifelog.application.settings.domain.group.InterfacePreferencesGroup

@Suppress("PropertyName")
interface AppSettings: ComposeKitSettings {
    val allGroups: List<PlatformSettingsGroup> get() =
        listOf(
            Database,
            DatabaseSource,
            Interface,
            Display
        )

    val prefs: PlatformSettings

    val Database: DatabasePreferencesGroup
    val DatabaseSource: DatabaseSourcePreferencesGroup
    override val Interface: InterfacePreferencesGroup
    val Display: DisplayPreferencesGroup
}
