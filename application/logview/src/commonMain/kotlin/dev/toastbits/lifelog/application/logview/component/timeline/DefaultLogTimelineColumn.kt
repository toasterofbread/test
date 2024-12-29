package dev.toastbits.lifelog.application.logview.component.timeline

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.platform.composable.BackHandler
import dev.toastbits.composekit.components.platform.composable.areScrollBarsVisible
import dev.toastbits.composekit.components.utils.modifier.horizontal
import dev.toastbits.composekit.util.composable.copy
import dev.toastbits.composekit.util.composable.thenIf
import dev.toastbits.lifelog.application.logview.component.timeline.model.LogTimelineScrollTarget
import dev.toastbits.lifelog.application.logview.component.timeline.model.LogTimelineState
import dev.toastbits.lifelog.application.logview.model.LogEventReference
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun DefaultLogTimelineColumn(
    contentPadding: PaddingValues,
    timelineState: LogTimelineState,
    logDatabase: LogDatabase,
    isEventSelected: (LogEventReference) -> Boolean,
    showSearchBar: Boolean,
    onAddEvent: (suspend () -> Unit)?,
    setShowSearchBar: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onEventSelected: ((LogEventReference) -> Unit)? = null,
    extraFloatingContent: @Composable () -> Unit = {},
    filterEvents: ((LogEvent, LogEventReference) -> Boolean)? = null,
    filterKey: Any? = Unit,
    scrollToItem: State<LogEventReference?>? = null
) {
    val density: Density = LocalDensity.current

    var currentDateIndex: Int? by remember { mutableStateOf(null) }
    var scrollTarget: LogTimelineScrollTarget? by remember { mutableStateOf(null) }
    var shouldFocusSearchBar: Boolean by remember { mutableStateOf(false) }

    BackHandler(showSearchBar) {
        setShowSearchBar(false)
    }

    LaunchedEffect(scrollToItem) {
        snapshotFlow { scrollToItem?.value }
            .collectLatest { item ->
                if (item == null) {
                    return@collectLatest
                }

                scrollTarget = LogTimelineScrollTarget.LogEvent(item)
            }
    }

    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            var navigationBarHeight: Dp by remember { mutableStateOf(0.dp) }
            var bottomContentHeight: Dp by remember { mutableStateOf(0.dp) }
            val scrollBarSpacing: Dp = 10.dp
            val scrollBarThickness: Dp = 8.dp

            LogTimeline(
                state = timelineState,
                logDatabase = logDatabase,
                isEventSelected = isEventSelected,
                modifier = Modifier.matchParentSize(),
                contentPadding = contentPadding.copy(bottom = bottomContentHeight),
                scrollBarContentPadding = contentPadding.copy(bottom = navigationBarHeight),
                scrollTarget = scrollTarget,
                onCurrentDateIndexChanged = {
                    currentDateIndex = it
                    scrollTarget = null
                },
                onEventSelected = onEventSelected,
                filterEvents = filterEvents,
                filterKey = filterKey,
                scrollBarSpacing = scrollBarSpacing,
                scrollBarThickness = scrollBarThickness
            )

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged {
                        with (density) {
                            bottomContentHeight = it.height.toDp()
                        }
                    }
            ) {
                val spacing: Dp = 12.dp

                Column(
                    Modifier
                        .padding(contentPadding.horizontal)
                        .thenIf(areScrollBarsVisible()) {
                            padding(horizontal = scrollBarSpacing + scrollBarThickness)
                        }
                ) {
                    extraFloatingContent()

                    androidx.compose.animation.AnimatedVisibility(
                        showSearchBar,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        LogTimelineSearchField(
                            timelineState,
                            shouldFocusSearchBar,
                            onClose = {
                                setShowSearchBar(false)
                                timelineState.filterText = null
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = spacing)
                        )
                    }
                }

                LogTimelineNavigationBar(
                    onAddEvent = onAddEvent,
                    canScrollUp = timelineState.columnState.canScrollBackward,
                    canScrollDown = timelineState.columnState.canScrollForward,
                    searching = showSearchBar,
                    scrollDateBy = { by ->
                        val current: Int = currentDateIndex ?: return@LogTimelineNavigationBar
                        if (by == Int.MAX_VALUE || by == Int.MIN_VALUE) {
                            scrollTarget = LogTimelineScrollTarget.DateIndex(by)
                        }
                        else {
                            scrollTarget = LogTimelineScrollTarget.DateIndex(current + by)
                        }
                    },
                    showSearchBar = {
                        setShowSearchBar(true)
                        shouldFocusSearchBar = true
                    },
                    modifier =
                        Modifier
                            .onSizeChanged {
                                with (density) {
                                    navigationBarHeight = it.height.toDp()
                                }
                            }
                            .padding(contentPadding.copy(top = spacing))
                )
            }
        }
    }
}
