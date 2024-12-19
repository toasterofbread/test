package dev.toastbits.lifelog.application.dbsource.data.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.theme.core.ThemeValues
import dev.toastbits.composekit.theme.core.ui.LocalComposeKitTheme
import dev.toastbits.composekit.util.thenWith
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceType
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_source_is_set_to_auto_open
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DatabaseSourceConfigurationPreview(
    configuration: DatabaseSourceConfiguration,
    modifier: Modifier = Modifier,
    autoOpens: Boolean = false,
    onSelected: (() -> Unit)? = null,
    tailItems: @Composable FlowRowScope.() -> Unit = {}
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
            .padding(15.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(type.getIcon(), type.getName())

            Column(
                Modifier.widthIn(min = 200.dp),
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

        FlowRow(
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                autoOpens,
                Modifier.align(Alignment.CenterVertically)
            ) {
                TooltipBox(
                    TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(Res.string.database_source_is_set_to_auto_open))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    Box(
                        Modifier
                            .minimumInteractiveComponentSize()
                            .size(IconButtonDefaults.smallContainerSize()),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            stringResource(Res.string.database_source_is_set_to_auto_open),
                            Modifier.alpha(0.75f)
                        )
                    }
                }
            }

            tailItems()
        }
    }
}
