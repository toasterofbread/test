package dev.toastbits.lifelog.application.logview.component.timeline

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
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
import dev.toastbits.composekit.components.utils.composable.wave.fullWavePath
import dev.toastbits.composekit.theme.core.ThemeValues
import dev.toastbits.composekit.theme.core.ui.LocalComposeKitTheme
import dev.toastbits.composekit.util.LocalLocale
import dev.toastbits.composekit.util.model.Locale
import dev.toastbits.lifelog.application.logview.component.timeline.item.DateTimelineItem
import dev.toastbits.lifelog.application.logview.component.timeline.item.EventTimelineItem
import dev.toastbits.lifelog.application.logview.component.timeline.item.TimelineItem
import dev.toastbits.lifelog.application.logview.component.timeline.item.rememberTimelineItems
import dev.toastbits.lifelog.application.logview.component.timeline.model.LogTimelineScrollTarget
import dev.toastbits.lifelog.application.logview.component.timeline.model.LogTimelineState
import dev.toastbits.lifelog.application.logview.model.LogEventReference
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.containsText
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val WAVE_SIZE: Dp = 15.dp
private val WAVE_THICKNESS: Dp = 1.5.dp
private val WAVE_WAVELENGTH: Dp = 40.dp
private const val WAVE_SCROLL_SPEED: Float = 0.75f
private val ITEM_SPACING: Dp = 25.dp

@Composable
internal fun LogTimeline(
    state: LogTimelineState,
    logDatabase: LogDatabase,
    isEventSelected: (LogEventReference) -> Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    scrollBarContentPadding: PaddingValues = contentPadding,
    scrollTarget: LogTimelineScrollTarget? = null,
    onCurrentDateIndexChanged: ((Int) -> Unit)? = null,
    onEventSelected: ((LogEventReference) -> Unit)? = null,
    filterEvents: ((LogEvent, LogEventReference) -> Boolean)? = null,
    scrollBarSpacing: Dp = 10.dp,
    scrollBarThickness: Dp = 8.dp,
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

    LaunchedEffect(scrollTarget) {
        val scrollIndex: Int =
            scrollTarget?.getTargetScrollIndex(timelineItems)
                ?: return@LaunchedEffect

        val dateIndex: Int =
            (0..scrollIndex).count { timelineItems.getOrNull(it) is DateTimelineItem } - 1

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
            scrollBarContentPadding = scrollBarContentPadding,
            onScrollDelta = { delta, _ ->
                state.waveOffset -= delta * WAVE_SCROLL_SPEED
            },
            scrollBarSpacing = scrollBarSpacing,
            scrollBarThickness = scrollBarThickness
        ) {
            for ((index, item) in timelineItems.withIndex()) {
                timelineItem(item, state, onEventSelected, isEventSelected)
            }
        }

        Canvas(
            Modifier
                .matchParentSize()
                .clipToBounds()
                .padding(contentPadding)
                .padding(end = scrollBarSpacing + scrollBarThickness)
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
                    fullWavePath(
                        path = path,
                        height = WAVE_SIZE.toPx(),
                        length = size.height,
                        waveLength = WAVE_WAVELENGTH.toPx(),
                        outerRotationDegrees = 0f,
                        offset = offset
                    )
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
    modifier: Modifier = Modifier
) {
    when (item) {
        is DateTimelineItem ->
            stickyHeaderContentPaddingAware(state.columnState, key = item.index) {
                LogTimelineItemPreview(
                    item = item,
                    onEventSelected = onEventSelected,
                    modifier = modifier
                )
            }

        is EventTimelineItem -> {
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
                    modifier = modifier.padding(bottom = ITEM_SPACING),
                    onEventSelected = onEventSelected
                )
            }
        }
    }
}

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
