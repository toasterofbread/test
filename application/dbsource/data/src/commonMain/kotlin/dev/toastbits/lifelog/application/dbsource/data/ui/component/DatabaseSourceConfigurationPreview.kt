package dev.toastbits.lifelog.application.dbsource.data.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.util.thenWith
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceType
import lifelog.application.dbsource.data.generated.resources.Res
import lifelog.application.dbsource.data.generated.resources.button_database_source_disable_auto_open
import lifelog.application.dbsource.data.generated.resources.database_source_is_set_to_auto_open
import org.jetbrains.compose.resources.stringResource

@Composable
fun DatabaseSourceConfigurationPreview(
    configuration: DatabaseSourceConfiguration,
    modifier: Modifier = Modifier,
    autoOpens: Boolean = false,
    onDisableAutoOpen: (() -> Unit)? = null,
    onSelected: (() -> Unit)? = null,
    tailContent: @Composable FlowRowScope.() -> Unit = {}
) {
    val type: DatabaseSourceType<*> = configuration.getType()
    val theme: ThemeValues = LocalComposeKitTheme.current
    val shape: CornerBasedShape = MaterialTheme.shapes.medium

    FlowRow(
        modifier
            .clip(shape)
            .thenWith(onSelected) {
                clickable(onClick = it)
            }
            .border(2.dp, theme.accent, shape)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
    ) {
        Row(
            Modifier.align(Alignment.CenterVertically).fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(type.getIcon(), type.getName())

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    configuration.getPreviewTitle(),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    configuration.getPreviewContent(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.7f)
                )
            }
        }

        AnimatedVisibility(autoOpens) {
            TooltipBox(
                TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(stringResource(Res.string.database_source_is_set_to_auto_open))
                    }
                },
                state = rememberTooltipState()
            ) {
                if (onDisableAutoOpen != null) {
                    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
                    val hovering: Boolean by interactionSource.collectIsHoveredAsState()
                    IconButton(onDisableAutoOpen, Modifier.hoverable(interactionSource)) {
                        if (hovering) {
                            Icon(Icons.Default.Close, stringResource(Res.string.button_database_source_disable_auto_open))
                        }
                        else {
                            Icon(Icons.Default.Flag, stringResource(Res.string.database_source_is_set_to_auto_open))
                        }
                    }
                }
                else {
                    Icon(Icons.Default.Flag, stringResource(Res.string.database_source_is_set_to_auto_open))
                }
            }
        }

        tailContent()
    }
}
