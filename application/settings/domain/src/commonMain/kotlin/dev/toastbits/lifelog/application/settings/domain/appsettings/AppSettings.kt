package dev.toastbits.lifelog.application.settings.domain.appsettings

import dev.toastbits.composekit.commonsettings.impl.ComposeKitSettings
import dev.toastbits.composekit.commonsettings.impl.group.ComposeKitSettingsGroupTheme
import dev.toastbits.composekit.settings.ComposeKitSettingsGroup
import dev.toastbits.lifelog.application.settings.domain.group.DatabasePreferencesGroup
import dev.toastbits.lifelog.application.settings.domain.group.DatabaseSourceSettingsGroup
import dev.toastbits.lifelog.application.settings.domain.group.DisplaySettingsGroup
import dev.toastbits.lifelog.application.settings.domain.group.InterfaceSettingsGroup

@Suppress("PropertyName")
interface AppSettings: ComposeKitSettings {
    override val allGroups: List<ComposeKitSettingsGroup> get() =
        listOf(
            Database,
            DatabaseSource,
            Interface,
            Theme,
            Display
        )

    val Database: DatabasePreferencesGroup
    val DatabaseSource: DatabaseSourceSettingsGroup
    override val Interface: InterfaceSettingsGroup
    override val Theme: ComposeKitSettingsGroupTheme
    val Display: DisplaySettingsGroup
}
