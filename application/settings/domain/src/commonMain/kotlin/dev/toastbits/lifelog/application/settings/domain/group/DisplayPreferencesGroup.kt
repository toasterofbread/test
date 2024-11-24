package dev.toastbits.lifelog.application.settings.domain.group

import dev.toastbits.composekit.settings.PlatformSettingsGroup
import dev.toastbits.composekit.settings.PlatformSettingsProperty
import dev.toastbits.lifelog.application.settings.domain.model.DisplayDateFormat

@Suppress("PropertyName")
interface DisplayPreferencesGroup: PlatformSettingsGroup {
    val DATE_FORMAT: PlatformSettingsProperty<DisplayDateFormat>
}
