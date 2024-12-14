package dev.toastbits.lifelog.application.settings.data.group

import dev.toastbits.composekit.commonsettings.impl.group.impl.ComposeKitSettingsGroupInterfaceImpl
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.lifelog.application.settings.data.generated.resources.Res
import dev.toastbits.lifelog.application.settings.data.generated.resources.language_name
import dev.toastbits.lifelog.application.settings.domain.group.InterfacePreferencesGroup

class InterfacePreferencesGroupImpl(
    preferences: PlatformSettings
): ComposeKitSettingsGroupInterfaceImpl(
    "INTERFACE",
    preferences,
    Res.string.language_name
), InterfacePreferencesGroup
