package dev.toastbits.lifelog.application.settings.data.group

import dev.toastbits.composekit.commonsettings.impl.group.impl.ComposeKitInterfacePlatformSettingsGroupImpl
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.lifelog.application.settings.domain.group.InterfacePreferencesGroup

class InterfacePreferencesGroupImpl(
    preferences: PlatformSettings
): ComposeKitInterfacePlatformSettingsGroupImpl("INTERFACE", preferences), InterfacePreferencesGroup
