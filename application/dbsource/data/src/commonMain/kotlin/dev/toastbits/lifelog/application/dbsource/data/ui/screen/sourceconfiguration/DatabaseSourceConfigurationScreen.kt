package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceconfiguration

import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.platform.composable.ScrollBarLazyColumn
import dev.toastbits.composekit.components.utils.composable.animatedvisibility.NullableValueAnimatedVisibility
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.composekit.settings.ui.component.item.SettingsItem
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.type.getLazyListConfigurationItems
import dev.toastbits.lifelog.application.settings.data.compositionlocal.LocalSettings
import lifelog.application.dbsource.data.generated.resources.Res
import lifelog.application.dbsource.data.generated.resources.button_database_source_auto_open
import lifelog.application.dbsource.data.generated.resources.button_database_source_auto_open_toggle
import org.jetbrains.compose.resources.stringResource

internal class DatabaseSourceConfigurationScreen<T: DatabaseSourceConfiguration>(
    private val initialConfiguration: T,
    private val index: Int,
    private val onSaved: (configuration: T, autoOpen: Boolean) -> Unit,
    private val onCancelled: () -> Unit,
    private val getSaveText: @Composable () -> String,
    private val getCancelText: @Composable () -> String
): Screen {
    @Composable
    override fun Content(navigator: Navigator, modifier: Modifier, contentPadding: PaddingValues) {
        val autoOpenIndex: Int by LocalSettings.current.DatabaseSource.AUTO_OPEN_SOURCE_INDEX.observe()

        val theme: ThemeValues = LocalComposeKitTheme.current
        var currentConfiguration: T by remember { mutableStateOf(initialConfiguration) }
        var saved: Boolean by remember { mutableStateOf(false) }
        var autoOpen: Boolean by remember { mutableStateOf(autoOpenIndex == index) }

        val invalidReasonMessages: Map<Int, String> = currentConfiguration.getInvalidReasonMessages()

        Column(
            modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val configurationItems: List<SettingsItem> =
                remember(currentConfiguration) {
                    currentConfiguration.getLazyListConfigurationItems { newConfiguration ->
                        currentConfiguration = newConfiguration
                    }
                }

            ScrollBarLazyColumn(
                modifier.fillMaxHeight().weight(1f),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                itemsIndexed(configurationItems) { index, item ->
                    Column {
                        item.Item(Modifier)

                        val invalidReasonMessage: String? = invalidReasonMessages[index]
                        NullableValueAnimatedVisibility(
                            invalidReasonMessage,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) { message ->
                            if (message != null) {
                                Text(message, style = MaterialTheme.typography.labelLarge, color = theme.error)
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val switchContentDescription: String = stringResource(Res.string.button_database_source_auto_open_toggle)
                Switch(
                    autoOpen,
                    { autoOpen = it },
                    Modifier.semantics { contentDescription = switchContentDescription }
                )

                Text(stringResource(Res.string.button_database_source_auto_open))

                Spacer(Modifier.fillMaxWidth().weight(1f))

                Button(onCancelled) {
                    Text(getCancelText())
                }

                Button(
                    {
                        if (saved || invalidReasonMessages.isNotEmpty()) {
                            return@Button
                        }
                        saved = true

                        onSaved(currentConfiguration, autoOpen)
                    },
                    enabled = invalidReasonMessages.isEmpty()
                ) {
                    Text(getSaveText())
                }
            }
        }
    }
}
