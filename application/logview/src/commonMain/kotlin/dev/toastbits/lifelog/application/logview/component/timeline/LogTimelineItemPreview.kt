package dev.toastbits.lifelog.application.logview.component.timeline

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.toastbits.composekit.components.utils.modifier.background
import dev.toastbits.composekit.components.utils.modifier.border
import dev.toastbits.composekit.theme.core.ThemeValues
import dev.toastbits.composekit.theme.core.ui.LocalComposeKitTheme
import dev.toastbits.composekit.theme.core.vibrantAccent
import dev.toastbits.composekit.util.thenIf
import dev.toastbits.composekit.util.thenWith
import dev.toastbits.lifelog.application.logview.component.timeline.item.EventTimelineItem
import dev.toastbits.lifelog.application.logview.component.timeline.item.TimelineItem
import dev.toastbits.lifelog.application.logview.model.LogEventReference
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal val ICON_COLUMN_WIDTH: Dp = 40.dp
internal const val START_COLUMN_FILL_RATIO: Float = 0.4f

private enum class SelectionState(val highlightOpacity: Float, val borderOpacity: Float) {
    SELECTED(0f, 1f),
    HOVERING(0.2f, 0f),
    NONE(0f, 0f)
}

@Composable
internal fun LogTimelineItemPreview(
    item: TimelineItem,
    modifier: Modifier = Modifier,
    selectedState: State<Boolean> = mutableStateOf(false),
    onEventSelected: ((LogEventReference) -> Unit)? = null
) {
    val theme: ThemeValues = LocalComposeKitTheme.current
    val shape: Shape = RoundedCornerShape(10.dp)

    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val hovering: Boolean by interactionSource.collectIsHoveredAsState()

    val highlightOpacity: Animatable<Float, AnimationVector1D> = remember { Animatable(0f) }
    val borderOpacity: Animatable<Float, AnimationVector1D> = remember { Animatable(0f) }
    var currentSelectionState: SelectionState by remember { mutableStateOf(SelectionState.NONE) }

    LaunchedEffect(selectedState) {
        snapshotFlow {
            if (selectedState.value) SelectionState.SELECTED
            else if (hovering) SelectionState.HOVERING
            else SelectionState.NONE
        }.collect { targetSelectionState ->
            launch {
                highlightOpacity.animateTo(targetSelectionState.highlightOpacity)
            }
            launch {
                borderOpacity.animateTo(targetSelectionState.borderOpacity)
            }
            currentSelectionState = targetSelectionState
        }
    }

    Row(
        modifier
            .fillMaxWidth()
            .thenWith(
                item as? EventTimelineItem,
                onEventSelected
            ) { event, callback ->
                clickable(interactionSource, null) {
                    callback(event.eventReference)
                }
                .hoverable(interactionSource)
            }
            .background(shape) {
                theme.vibrantAccent.copy(alpha = highlightOpacity.value)
            }
            .border(2.dp, shape) {
                theme.vibrantAccent.copy(alpha = borderOpacity.value)
            }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var mainContentHeight: Int by remember { mutableIntStateOf(0) }
        var iconContentHeight: Int by remember { mutableIntStateOf(0) }
        var contentIsSingleLine: Boolean by remember { mutableStateOf(false) }

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
                            iconContentHeight = it.height
                        }
                        .zIndex(1f)
                        .thenIf(!item.hasWideIcon) {
                            offset {
                                IntOffset(
                                    x = (-10).dp.roundToPx(), // ?
                                    y =
                                        if (contentIsSingleLine) 0
                                        else ((iconContentHeight - mainContentHeight) / 2f).roundToInt()
                                )
                            }
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
                        mainContentHeight = it.height
                    },
                onIsSingleLineChanged = {
                    contentIsSingleLine = it
                }
            )
        }
    }
}
