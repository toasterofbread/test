package dev.toastbits.lifelog.application.settings.data.appsettings

import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.lifelog.application.settings.data.group.DatabasePreferencesGroupImpl
import dev.toastbits.lifelog.application.settings.domain.appsettings.AppSettings
import dev.toastbits.lifelog.application.settings.domain.group.DatabaseSourcePreferencesGroup
import dev.toastbits.lifelog.application.settings.data.group.DatabaseSourcePreferencesGroupImpl
import dev.toastbits.lifelog.application.settings.data.group.DisplayPreferencesGroupImpl
import dev.toastbits.lifelog.application.settings.data.group.InterfacePreferencesGroupImpl
import dev.toastbits.lifelog.application.settings.domain.group.DatabasePreferencesGroup
import dev.toastbits.lifelog.application.settings.domain.group.DisplayPreferencesGroup
import dev.toastbits.lifelog.application.settings.domain.group.InterfacePreferencesGroup

class AppSettingsImpl(
    override val prefs: PlatformSettings
): AppSettings {
    override val Database: DatabasePreferencesGroup = DatabasePreferencesGroupImpl(prefs)
    override val DatabaseSource: DatabaseSourcePreferencesGroup = DatabaseSourcePreferencesGroupImpl(prefs)
    override val Interface: InterfacePreferencesGroup = InterfacePreferencesGroupImpl(prefs)
    override val Display: DisplayPreferencesGroup = DisplayPreferencesGroupImpl(prefs)
}
