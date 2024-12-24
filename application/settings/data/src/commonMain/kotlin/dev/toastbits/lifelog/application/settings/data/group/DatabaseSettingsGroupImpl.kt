package dev.toastbits.lifelog.application.settings.data.group

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.toastbits.composekit.settings.ComposeKitSettingsGroupImpl
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.composekit.settingsitem.domain.PlatformSettingsProperty
import dev.toastbits.composekit.settingsitem.domain.SettingsItem
import dev.toastbits.composekit.settingsitem.presentation.ui.component.item.DropdownSettingsItem
import dev.toastbits.lifelog.application.settings.data.generated.resources.Res
import dev.toastbits.lifelog.application.settings.data.generated.resources.pref_database_split_strategy_description
import dev.toastbits.lifelog.application.settings.data.generated.resources.pref_database_split_strategy_title
import dev.toastbits.lifelog.application.settings.data.generated.resources.prefs_group_database_description
import dev.toastbits.lifelog.application.settings.data.generated.resources.prefs_group_database_title
import dev.toastbits.lifelog.application.settings.domain.group.DatabasePreferencesGroup
import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.database.LogFileSplitStrategy
import dev.toastbits.lifelog.core.specification.extension.ExtensionRegistry
import dev.toastbits.lifelog.core.specification.impl.converter.LogFileConverterStringsImpl
import dev.toastbits.lifelog.core.specification.impl.extension.ExtensionRegistryImpl
import org.jetbrains.compose.resources.stringResource

class DatabaseSettingsGroupImpl(preferences: PlatformSettings): ComposeKitSettingsGroupImpl("DATABASE_SOURCE", preferences), DatabasePreferencesGroup {
    @Composable
    override fun getTitle(): String = stringResource(Res.string.prefs_group_database_title)

    @Composable
    override fun getDescription(): String = stringResource(Res.string.prefs_group_database_description)

    @Composable
    override fun getIcon(): ImageVector = Icons.Default.Storage

    override val extensionRegistry: ExtensionRegistry = ExtensionRegistryImpl()
    override val logFileConverterStrings: LogFileConverterStrings = LogFileConverterStringsImpl()

    override val SPLIT_STRATEGY: PlatformSettingsProperty<LogFileSplitStrategy> by
        enumProperty(
            getName = { stringResource(Res.string.pref_database_split_strategy_title) },
            getDescription = { stringResource(Res.string.pref_database_split_strategy_description) },
            getDefaultValue = { LogFileSplitStrategy.Month }
        )

    override fun getConfigurationItems(): List<SettingsItem> =
        listOf(
            DropdownSettingsItem.ofEnumState(SPLIT_STRATEGY) {
                it.toString()
            }
        )
}
