package dev.toastbits.lifelog.application.logview.data.ui.component.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.toastbits.composekit.util.thenIf
import dev.toastbits.composekit.util.thenWith
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.item.EventTimelineItem
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.item.TimelineItem
import dev.toastbits.lifelog.application.logview.data.ui.model.LogEventReference

internal val ICON_COLUMN_WIDTH: Dp = 40.dp
internal const val START_COLUMN_FILL_RATIO: Float = 0.4f

@Composable
internal fun VerticalLogTimelineItemPreview(
    item: TimelineItem,
    modifier: Modifier = Modifier,
    onEventSelected: ((LogEventReference) -> Unit)? = null
) {
    val density: Density = LocalDensity.current

    Row(
        modifier
            .fillMaxWidth()
            .thenWith(
                item as? EventTimelineItem,
                onEventSelected
            ) { event, callback ->
                clip(RoundedCornerShape(10.dp))
                    .clickable {
                        callback(event.eventReference)
                    }
            }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var mainContentHeight: Dp by remember { mutableStateOf(0.dp) }
        var iconContentHeight: Dp by remember { mutableStateOf(0.dp) }

        Row(
            Modifier.fillMaxWidth(START_COLUMN_FILL_RATIO),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!item.hasWideIcon) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    item.MetadataItems(Modifier)
                }
            }

            Box(
                if (item.hasWideIcon) Modifier.fillMaxWidth()
                else Modifier.width(ICON_COLUMN_WIDTH),
                contentAlignment = Alignment.Center
            ) {
                item.IconContent(
                    Modifier
                        .onSizeChanged {
                            with(density) {
                                iconContentHeight = it.height.toDp()
                            }
                        }
                        .zIndex(1f)
                        .thenIf(!item.hasWideIcon) {
                            offset(
                                x = (-10).dp, // ?
                                y = (iconContentHeight - mainContentHeight) / 2f
                            )
                        }
                )
            }
        }

        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.titleSmall,
            LocalContentColor provides LocalContentColor.current.copy(alpha = 0.7f)
        ) {
            item.MainContent(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp)
                    .onSizeChanged {
                        mainContentHeight = with(density) { it.height.toDp() }
                    }
            )
        }
    }
}
