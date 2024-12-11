package dev.toastbits.lifelog.application.settings.data.group

import dev.toastbits.composekit.commonsettings.impl.group.impl.ComposeKitSettingsGroupThemeImpl
import dev.toastbits.composekit.settings.PlatformSettings

class ThemePreferencesGroupImpl(
    preferences: PlatformSettings
): ComposeKitSettingsGroupThemeImpl("THEME", preferences)
