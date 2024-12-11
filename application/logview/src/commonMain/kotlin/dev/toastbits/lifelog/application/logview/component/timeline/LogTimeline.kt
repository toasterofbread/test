package dev.toastbits.lifelog.application.logview.component.timeline

import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import dev.toastbits.composekit.theme.vibrantAccent
import dev.toastbits.composekit.util.LocalLocale
import dev.toastbits.composekit.util.model.Locale
import dev.toastbits.lifelog.application.logview.component.timeline.item.DateTimelineItem
import dev.toastbits.lifelog.application.logview.component.timeline.item.EventTimelineItem
import dev.toastbits.lifelog.application.logview.component.timeline.item.TimelineItem
import dev.toastbits.lifelog.application.logview.component.timeline.item.rememberTimelineItems
import dev.toastbits.lifelog.application.logview.component.timeline.model.LogTimelineState
import dev.toastbits.lifelog.application.logview.model.LogEventReference
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.containsText
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
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
private val SCROLLBAR_THICKNESS: Dp = 8.dp
private val SCROLLBAR_SPACING: Dp = 5.dp

@Composable
internal fun LogTimeline(
    state: LogTimelineState,
    logDatabase: LogDatabase,
    isEventSelected: (LogEventReference) -> Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    scrollTargetDateIndex: Int? = null,
    onCurrentDateIndexChanged: ((Int) -> Unit)? = null,
    onEventSelected: ((LogEventReference) -> Unit)? = null,
    filterEvents: ((LogEvent, LogEventReference) -> Boolean)? = null,
    filterKey: Any? = Unit
) {
    val theme: ThemeValues = LocalComposeKitTheme.current
    val density: Density = LocalDensity.current
    val locale: Locale = LocalLocale.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    val timelineItems: List<TimelineItem> by
        logDatabase.rememberTimelineItems(
            key1 = state.filterText,
            key2 = locale,
            key3 = filterKey,
            filterEvents = { event, reference ->
                if (filterEvents?.invoke(event, reference) == false) {
                    return@rememberTimelineItems false
                }

                if (state.filterText?.let { event.containsText(it, locale) } == false) {
                    return@rememberTimelineItems false
                }

                return@rememberTimelineItems true
            }
        )
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
        ScrollBarLazyColumn(
            Modifier.fillMaxSize(),
            state = state.columnState,
            contentPadding = contentPadding,
            onScrollDelta = { delta, _ ->
                state.waveOffset -= delta * WAVE_SCROLL_SPEED
            },
            scrollBarSpacing = SCROLLBAR_SPACING,
            scrollBarThickness = SCROLLBAR_THICKNESS
        ) {
            for (item in timelineItems) {
                timelineItem(item, state, onEventSelected, isEventSelected)
            }
        }

        Canvas(
            Modifier
                .matchParentSize()
                .clipToBounds()
                .padding(contentPadding)
                .padding(end = SCROLLBAR_THICKNESS + SCROLLBAR_SPACING)
                .zIndex(-1f)
        ) {
            val position: Float =
                (size.width * START_COLUMN_FILL_RATIO).let { startColumnWidth ->
                    startColumnWidth - (ICON_COLUMN_WIDTH.toPx() / 2f)
                }

            translate(left = position - (WAVE_SIZE.toPx() / 2f)) {
                rotate(90f, pivot = Offset.Zero) {
                    val path: Path = Path()
                    val maxOffset: Float = 100f
                    val offset: Float = ((waveOffset % maxOffset) / maxOffset).let { if (it < 0f) 1f + it else it }
                    fullWavePath(path, WAVE_SIZE.toPx(), WAVE_WAVELENGTH.toPx(), size.height, 0f, offset)
                    drawPath(path, theme.accent, style = Stroke(WAVE_THICKNESS.toPx()))
                }
            }
        }
    }
}

private fun LazyListScope.timelineItem(
    item: TimelineItem,
    state: LogTimelineState,
    onEventSelected: ((LogEventReference) -> Unit)?,
    isEventSelected: (LogEventReference) -> Boolean,
) {
    when (item) {
        is DateTimelineItem ->
            stickyHeaderContentPaddingAware(state.columnState, key = item.index) {
                LogTimelineItemPreview(
                    item = item,
                    onEventSelected = onEventSelected
                )
            }

        is EventTimelineItem ->
            item(key = item.eventReference.hashCode().toString()) {
                val isSelected: State<Boolean> =
                    remember(item.eventReference) {
                        derivedStateOf {
                            isEventSelected(item.eventReference)
                        }
                    }

                LogTimelineItemPreview(
                    item = item,
                    selectedState = isSelected,
                    modifier = Modifier.padding(bottom = ITEM_SPACING),
                    onEventSelected = onEventSelected
                )
            }
    }
}

fun fullWavePath(
    path: Path,
    height: Float,
    waveLength: Float,
    length: Float,
    outerRotationDegrees: Float = 0f,
    offset: Float = 0f
): Path {
    for (direction in listOf(-1, 1)) {
        newWavePath(path, direction, height, waveLength, length, outerRotationDegrees, offset)
    }
    return path
}

// TODO | Move back to ComposeKit
fun newWavePath(
    path: Path,
    direction: Int,
    height: Float,
    waveLength: Float,
    length: Float,
    outerRotationDegrees: Float = 0f,
    offset: Float = 0f
): Path {
    require(offset in 0f .. 1f) { offset }

    val halfPeriod: Float = waveLength / 2f
    val effectiveWidth: Float = ceil(length / halfPeriod) * halfPeriod

    val rotationAdj: Float = sin(outerRotationDegrees.toRadians())
    val yOffset: Float = -(length * rotationAdj * 0.5f)

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

private suspend fun LogEvent.containsText(text: String, locale: Locale): Boolean {
    for (content in getAllUserContent()) {
        if (content.containsText(text, ignoreCase = true)) {
            return true
        }
    }

    when (val title: LogDisplayText? = getTitle(locale)) {
        null -> {}
        is LogDisplayText.OfString ->
            if (title.string.contains(text, ignoreCase = true)) {
                return true
            }
        is LogDisplayText.OfUserContent ->
            if (title.userContent.containsText(text, ignoreCase = true)) {
                return true
            }
    }

    return false
}

@Composable
fun WaveBorder(
    modifier: Modifier = Modifier,
    waveColour: Color = LocalComposeKitTheme.current.vibrantAccent,
    waveLength: Dp = 70.dp,
    waveThickness: Dp = 2.dp,
    animation: DurationBasedAnimationSpec<Float>? = tween(2000, easing = LinearEasing)
) {
    val offset: Float by
        animation?.let {
            rememberInfiniteTransition().animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = it,
                    repeatMode = RepeatMode.Restart
                )
            )
        } ?: mutableStateOf(0f)

    Canvas(
        modifier
            .height(15.dp)
            .clipToBounds()
    ) {
        val path: Path = Path()

        fullWavePath(
            path,
            size.height,
            waveLength.toPx(),
            size.width,
            offset = offset
        )

        translate(top = size.height / 2f) {
            drawPath(path, waveColour, style = Stroke(waveThickness.toPx()))
        }
    }
}

//@Composable
//fun WaveBorderBox(
//    waveColour: Color,
//    waveLength: Dp,
//    waveThickness: Dp,
//    modifier: Modifier = Modifier,
//    waveHeight: Dp = 10.dp,
//    content: @Composable BoxScope.(PaddingValues) -> Unit
//) {
//    val infiniteTransition: InfiniteTransition = rememberInfiniteTransition()
//    val offset: Float by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 1f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(1500, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        )
//    )
//
//    Box(modifier) {
//        Canvas(
//            Modifier
//                .matchParentSize()
//                .clip(RoundedCornerShape(35.dp))
//        ) {
//            val path: Path = Path()
//
//            fullWavePath(
//                path,
//                waveHeight.toPx(),
//                waveLength.toPx(),
//                size.width,
//                offset = offset
//            )
//
//            clipRect {
//                val halfWaveHeight: Float = waveHeight.toPx() / 2f
//                val halfWaveLength: Float = waveLength.toPx() / 2f
//
//                translate(top = halfWaveHeight) {
//                    drawPath(path, waveColour, style = Stroke(waveThickness.toPx()))
//                }
//
//                translate(top = size.height - halfWaveHeight, left = halfWaveLength) {
//                    rotate(180f, pivot = Offset.Zero) {
//                        drawPath(path, waveColour, style = Stroke(waveThickness.toPx()))
//                    }
//                }
//
//                path.reset()
//                fullWavePath(
//                    path,
//                    waveHeight.toPx(),
//                    waveLength.toPx(),
//                    size.height,
//                    offset = offset
//                )
//
//                translate(left = halfWaveHeight, top = halfWaveLength) {
//                    rotate(270f, pivot = Offset.Zero) {
//                        drawPath(path, waveColour, style = Stroke(waveThickness.toPx()))
//                    }
//                }
//
//                translate(left = size.width - halfWaveHeight) {
//                    rotate(90f, pivot = Offset.Zero) {
//                        drawPath(path, waveColour, style = Stroke(waveThickness.toPx()))
//                    }
//                }
//            }
//        }
//
//        Box {
//            content(PaddingValues(waveHeight + 5.dp))
//        }
//    }
//}
