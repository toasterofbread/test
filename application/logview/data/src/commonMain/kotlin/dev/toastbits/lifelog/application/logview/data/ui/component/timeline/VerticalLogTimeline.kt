package dev.toastbits.lifelog.application.logview.data.ui.component.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.toastbits.composekit.components.platform.composable.ScrollBarLazyColumn
import dev.toastbits.composekit.components.utils.composable.stickyHeaderContentPaddingAware
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.composekit.util.thenIf
import dev.toastbits.composekit.util.thenWith
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
private const val METADATA_COLUMN_FILL_RATIO: Float = 0.7f
private const val START_COLUMN_FILL_RATIO: Float = 0.4f

@Composable
internal fun VerticalLogTimeline(
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
    val columnState: LazyListState = rememberLazyListState()

    var prev: Pair<Int, Int> by remember { mutableStateOf(0 to 0) }

    val current = columnState.firstVisibleItemIndex to columnState.firstVisibleItemScrollOffset
    var waveOffset: Float by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(current) {
        val delta: Int =
            if (current.first == prev.first) prev.second - current.second
            else 0//columnState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.times(prev.first - current.first) ?: 0
        prev = current
        waveOffset += delta * WAVE_SCROLL_SPEED
    }

    if (onCurrentDateIndexChanged != null) {
        val currentDateIndex: Int? =
            columnState.layoutInfo.visibleItemsInfo
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
            columnState.animateScrollToItem(
                index = scrollIndex,
                scrollOffset =
                    if (scrollIndex == 0) 0
                    else with (density) { contentPadding.calculateTopPadding().roundToPx() + 5 }
            )
        }
    }

    Box(modifier) {
        ScrollBarLazyColumn(
            Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = contentPadding
        ) {
            for (item in timelineItems) {
                when (item) {
                    is DateTimelineItem ->
                        stickyHeaderContentPaddingAware(columnState, key = item.index) {
                            Item(item, onEventSelected, density)
                        }
                    is EventTimelineItem ->
                        item(key = item.event.hashCode().toString()) {
                            Item(item, onEventSelected, density)
                        }
                }
            }
        }

        Canvas(
            Modifier
                .matchParentSize()
                .clipToBounds()
                .padding(contentPadding)
                .zIndex(-1f)
        ) {
            val position: Float =
                (size.width * START_COLUMN_FILL_RATIO).let { startColumnWidth ->
                    startColumnWidth * (METADATA_COLUMN_FILL_RATIO + ((1f - METADATA_COLUMN_FILL_RATIO) / 2f))
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

@Composable
private fun Item(
    item: TimelineItem,
    onEventSelected: ((LogEventReference) -> Unit)?,
    density: Density
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .thenWith(
                item as? EventTimelineItem,
                onEventSelected
            ) { event, callback ->
                clip(RoundedCornerShape(10.dp))
                    .clickable {
                        callback(event.event)
                    }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        var mainContentHeight: Dp by remember { mutableStateOf(0.dp) }
        var iconContentHeight: Dp by remember { mutableStateOf(0.dp) }

        Row(
            Modifier.fillMaxWidth(START_COLUMN_FILL_RATIO),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!item.hasWideIcon) {
                Column(Modifier.fillMaxWidth(METADATA_COLUMN_FILL_RATIO)) {
                    item.MetadataItems(Modifier)
                }
            }

            Box(
                Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                item.IconContent(
                    Modifier
                        .onSizeChanged {
                            iconContentHeight = with(density) { it.height.toDp() }
                        }
                        .zIndex(1f)
                        .thenIf(!item.hasWideIcon) {
                            offset(y = (iconContentHeight - mainContentHeight) / 2f)
                        }
                )

                if (item.hasWideIcon) {
                    Box(Modifier.fillMaxWidth(METADATA_COLUMN_FILL_RATIO))
                }
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

