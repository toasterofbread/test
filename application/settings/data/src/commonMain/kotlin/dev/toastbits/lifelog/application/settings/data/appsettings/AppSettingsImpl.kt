package dev.toastbits.lifelog.application.settings.data.appsettings

import dev.toastbits.composekit.commonsettings.impl.group.ComposeKitSettingsGroupTheme
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.lifelog.application.settings.data.group.DatabaseSettingsGroupImpl
import dev.toastbits.lifelog.application.settings.data.group.DatabaseSourceSettingsGroupImpl
import dev.toastbits.lifelog.application.settings.data.group.DisplaySettingsGroupImpl
import dev.toastbits.lifelog.application.settings.data.group.InterfaceSettingsGroupImpl
import dev.toastbits.lifelog.application.settings.data.group.ThemePreferencesGroupImpl
import dev.toastbits.lifelog.application.settings.domain.appsettings.AppSettings
import dev.toastbits.lifelog.application.settings.domain.group.DatabasePreferencesGroup
import dev.toastbits.lifelog.application.settings.domain.group.DatabaseSourceSettingsGroup
import dev.toastbits.lifelog.application.settings.domain.group.DisplaySettingsGroup
import dev.toastbits.lifelog.application.settings.domain.group.InterfaceSettingsGroup

class AppSettingsImpl(
    override val preferences: PlatformSettings
): AppSettings {
    override val Database: DatabasePreferencesGroup = DatabaseSettingsGroupImpl(preferences)
    override val DatabaseSource: DatabaseSourceSettingsGroup = DatabaseSourceSettingsGroupImpl(preferences)
    override val Interface: InterfaceSettingsGroup = InterfaceSettingsGroupImpl(preferences)
    override val Theme: ComposeKitSettingsGroupTheme = ThemePreferencesGroupImpl(preferences)
    override val Display: DisplaySettingsGroup = DisplaySettingsGroupImpl(preferences)
}
