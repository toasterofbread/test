package dev.toastbits.lifelog.application.logview.data.ui.component.timeline

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.toastbits.composekit.components.platform.composable.ScrollBarLazyColumn
import dev.toastbits.composekit.components.utils.composable.stickyHeaderContentPaddingAware
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.item.DateTimelineItem
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.item.EventTimelineItem
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.item.TimelineItem
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.item.rememberTimelineItems
import dev.toastbits.lifelog.application.logview.data.ui.screen.LogEventReference
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.sin

private val WAVE_SIZE: Dp = 15.dp
private val WAVE_THICKNESS: Dp = 1.5.dp
private val WAVE_WAVELENGTH: Dp = 40.dp
private const val WAVE_SCROLL_SPEED: Float = 0.75f
private val ITEM_SPACING: Dp = 25.dp

internal class VerticalLogTimelineState(
    from: VerticalLogTimelineState? = null
) {
    val columnState: LazyListState =
        LazyListState(
            from?.columnState?.firstVisibleItemIndex ?: 0,
            from?.columnState?.firstVisibleItemScrollOffset ?: 0
        )
    var waveOffset: Float by mutableFloatStateOf(from?.waveOffset ?: 0f)
}

@Composable
internal fun VerticalLogTimeline(
    state: VerticalLogTimelineState,
    logDatabase: LogDatabase,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    scrollTargetDateIndex: Int? = null,
    onCurrentDateIndexChanged: ((Int) -> Unit)? = null,
    onEventSelected: ((LogEventReference) -> Unit)? = null
) {
    val theme: ThemeValues = LocalComposeKitTheme.current
    val density: Density = LocalDensity.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    val timelineItems: List<TimelineItem> = logDatabase.rememberTimelineItems()
    val waveOffset: Float by animateFloatAsState(state.waveOffset)

    if (onCurrentDateIndexChanged != null) {
        val currentDateIndex: Int? =
            state.columnState.layoutInfo.visibleItemsInfo
                .filter { it.key is Int && it.offset <= 0 }
                .maxByOrNull { it.offset }?.key as Int?

        LaunchedEffect(currentDateIndex) {
            if (currentDateIndex != null) {
                onCurrentDateIndexChanged(currentDateIndex)
            }
        }
    }

    LaunchedEffect(scrollTargetDateIndex) {
        if (scrollTargetDateIndex == null) {
            return@LaunchedEffect
        }

        val dateIndex: Int =
            if (scrollTargetDateIndex == Int.MAX_VALUE)
                (timelineItems.lastOrNull { it is DateTimelineItem } as DateTimelineItem?)?.index ?: return@LaunchedEffect
            else if (scrollTargetDateIndex == Int.MIN_VALUE) 0
            else scrollTargetDateIndex

        val scrollIndex: Int =
            timelineItems.indexOfFirst { item ->
                (item as? DateTimelineItem)?.index == dateIndex
            }

        if (scrollIndex == -1) {
            return@LaunchedEffect
        }

        onCurrentDateIndexChanged?.invoke(dateIndex)

        coroutineScope.launch {
            val delta: Int = scrollIndex - state.columnState.firstVisibleItemIndex
            state.waveOffset -= delta * (state.columnState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0)

            state.columnState.animateScrollToItem(
                index = scrollIndex,
                scrollOffset =
                    if (scrollIndex == 0) 0
                    else with (density) { contentPadding.calculateTopPadding().roundToPx() + 5 }
            )
        }
    }

    Box(modifier) {
        val scrollBarThickness: Dp = 8.dp
        val scrollBarSpacing: Dp = 5.dp

        ScrollBarLazyColumn(
            Modifier.fillMaxSize(),
            state = state.columnState,
            contentPadding = contentPadding,
            onScrollDelta = { delta, _ ->
                state.waveOffset -= delta * WAVE_SCROLL_SPEED
            },
            scrollBarSpacing = scrollBarSpacing,
            scrollBarThickness = scrollBarThickness
        ) {
            for (item in timelineItems) {
                when (item) {
                    is DateTimelineItem ->
                        stickyHeaderContentPaddingAware(state.columnState, key = item.index) {
                            VerticalLogTimelineItemPreview(
                                item,
                                onEventSelected = onEventSelected
                            )
                        }
                    is EventTimelineItem ->
                        item(key = item.eventReference.hashCode().toString()) {
                            VerticalLogTimelineItemPreview(
                                item,
                                Modifier.padding(bottom = ITEM_SPACING),
                                onEventSelected = onEventSelected
                            )
                        }
                }
            }
        }

        Canvas(
            Modifier
                .matchParentSize()
                .clipToBounds()
                .padding(contentPadding)
                .padding(end = scrollBarThickness + scrollBarSpacing)
                .zIndex(-1f)
        ) {
            val position: Float =
                (size.width * START_COLUMN_FILL_RATIO).let { startColumnWidth ->
                    startColumnWidth - (ICON_COLUMN_WIDTH.toPx() / 2f)
                }

            translate(left = position - (WAVE_SIZE.toPx() / 2f)) {
                rotate(90f, pivot = Offset.Zero) {
                    val path: Path = Path()
                    for (direction in listOf(-1, 1)) {
                        val maxOffset = 100f
                        val offset: Float = ((waveOffset % maxOffset) / maxOffset).let { if (it < 0f) 1f + it else it }
                        wavePath(path, direction, WAVE_SIZE.toPx(), WAVE_WAVELENGTH, 0f, offset)
                        drawPath(path, theme.accent, style = Stroke(WAVE_THICKNESS.toPx()))
                    }
                }
            }
        }
    }
}

private fun DrawScope.wavePath(
    path: Path,
    direction: Int,
    height: Float,
    wavelength: Dp,
    outerRotationDegrees: Float = 0f,
    offset: Float
): Path {
    path.reset()

    val halfPeriod: Float = wavelength.toPx() / 2

    val rotationAdj: Float = sin(outerRotationDegrees.toRadians())
    val maxSize: Float = maxOf(size.width, size.height)

    val effectiveWidth: Float = ceil(maxSize / halfPeriod) * halfPeriod

    val yOffset: Float = -(maxSize * rotationAdj * 0.5f)

    check(offset in 0f .. 1f) { offset }

    val xOffset: Float = offset * halfPeriod * 2
    val xAdjustedOffset = (xOffset % effectiveWidth) - (if (xOffset > 0f) effectiveWidth else 0f)
    path.moveTo(x = -halfPeriod / 2 + xAdjustedOffset, y = yOffset)

    for (i in 0 until ceil((effectiveWidth * 2) / halfPeriod + 1).toInt()) {
        if ((i % 2 == 0) != (direction == 1)) {
            path.relativeMoveTo(halfPeriod, 0f)
            continue
        }

        path.relativeQuadraticTo(
            dx1 = halfPeriod / 2,
            dy1 = height / 2 * direction,
            dx2 = halfPeriod,
            dy2 = 0f
        )
    }

    return path
}

private fun Float.toRadians(): Float =
    (this * 180f) / PI.toFloat()

