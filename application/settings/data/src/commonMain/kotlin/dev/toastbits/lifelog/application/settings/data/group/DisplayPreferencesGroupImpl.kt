package dev.toastbits.lifelog.application.settings.data.group

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.composekit.settings.ComposeKitSettingsGroupImpl
import dev.toastbits.composekit.settings.PlatformSettingsProperty
import dev.toastbits.composekit.settings.ui.component.item.DropdownSettingsItem
import dev.toastbits.composekit.settings.ui.component.item.SettingsItem
import dev.toastbits.lifelog.application.settings.domain.group.DisplayPreferencesGroup
import dev.toastbits.lifelog.application.settings.domain.model.DisplayDateFormat
import dev.toastbits.lifelog.application.settings.data.generated.resources.Res
import dev.toastbits.lifelog.application.settings.data.generated.resources.pref_display_date_format_title
import dev.toastbits.lifelog.application.settings.data.generated.resources.prefs_group_display_description
import dev.toastbits.lifelog.application.settings.data.generated.resources.prefs_group_display_title
import org.jetbrains.compose.resources.stringResource

class DisplayPreferencesGroupImpl(preferences: PlatformSettings): ComposeKitSettingsGroupImpl("DISPLAY", preferences), DisplayPreferencesGroup {
    @Composable
    override fun getTitle(): String = stringResource(Res.string.prefs_group_display_title)

    @Composable
    override fun getDescription(): String = stringResource(Res.string.prefs_group_display_description)

    @Composable
    override fun getIcon(): ImageVector = Icons.Default.Visibility

    override val DATE_FORMAT: PlatformSettingsProperty<DisplayDateFormat> by
        enumProperty(
            getName = { stringResource(Res.string.pref_display_date_format_title) },
            getDescription = { null },
            getDefaultValue = { DisplayDateFormat.DEFAULT }
        )

    override fun getConfigurationItems(): List<SettingsItem> =
        listOf(
            DropdownSettingsItem.ofEnumState(DATE_FORMAT) {
                it.toString()
            }
        )
}
