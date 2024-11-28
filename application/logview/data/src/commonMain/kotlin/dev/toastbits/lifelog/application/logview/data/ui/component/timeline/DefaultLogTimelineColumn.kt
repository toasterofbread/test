package dev.toastbits.lifelog.application.logview.data.ui.component.timeline

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.utils.modifier.horizontal
import dev.toastbits.composekit.util.copy
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.model.LogTimelineState
import dev.toastbits.lifelog.application.logview.data.ui.model.LogEventReference
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent

@Composable
internal fun DefaultLogTimelineColumn(
    contentPadding: PaddingValues,
    timelineState: LogTimelineState,
    logDatabase: LogDatabase,
    showSearchBar: Boolean,
    setShowSearchBar: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onEventSelected: ((LogEventReference) -> Unit)? = null,
    extraFloatingContent: @Composable () -> Unit = {},
    filterEvents: ((LogEvent, LogEventReference) -> Boolean)? = null,
    filterKey: Any? = Unit
) {
    val density: Density = LocalDensity.current

    var currentDateIndex: Int? by remember { mutableStateOf(null) }
    var scrollTargetDateIndex: Int? by remember { mutableStateOf(null) }
    var shouldFocusSearchBar: Boolean by remember { mutableStateOf(false) }

    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            var bottomContentHeight: Dp by remember { mutableStateOf(0.dp) }

            LogTimeline(
                timelineState,
                logDatabase,
                Modifier.matchParentSize(),
                contentPadding = contentPadding.copy(bottom = bottomContentHeight),
                scrollTargetDateIndex = scrollTargetDateIndex,
                onCurrentDateIndexChanged = {
                    currentDateIndex = it
                    scrollTargetDateIndex = null
                },
                onEventSelected = onEventSelected,
                filterEvents = filterEvents,
                filterKey = filterKey
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
                extraFloatingContent()

                androidx.compose.animation.AnimatedVisibility(
                    showSearchBar,
                    enter = slideInVertically { it / 2 } + fadeIn(),
                    exit = slideOutVertically { it / 2 } + fadeOut()
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
                            .padding(contentPadding.horizontal)
                    )
                }
            }
        }

        LogTimelineNavigationBar(
            canScrollUp = timelineState.columnState.canScrollBackward,
            canScrollDown = timelineState.columnState.canScrollForward,
            searching = showSearchBar,
            currentDateIndex = currentDateIndex,
            setScrollTargetDateIndex = {
                scrollTargetDateIndex = it
            },
            showSearchBar = {
                setShowSearchBar(true)
                shouldFocusSearchBar = true
            },
            modifier = Modifier.padding(contentPadding.copy(top = 0.dp))
        )
    }
}
