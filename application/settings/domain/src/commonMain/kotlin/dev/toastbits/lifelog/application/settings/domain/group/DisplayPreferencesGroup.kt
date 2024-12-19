package dev.toastbits.lifelog.application.settings.domain.group

import dev.toastbits.composekit.settings.ComposeKitSettingsGroup
import dev.toastbits.composekit.settingsitem.domain.PlatformSettingsProperty
import dev.toastbits.lifelog.application.settings.domain.model.DisplayDateFormat

@Suppress("PropertyName")
interface DisplayPreferencesGroup: ComposeKitSettingsGroup {
    val DATE_FORMAT: PlatformSettingsProperty<DisplayDateFormat>
}
