package dev.toastbits.lifelog.application.logview.data.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.platform.composable.BackHandler
import dev.toastbits.composekit.components.platform.composable.ScrollBarLazyColumn
import dev.toastbits.composekit.components.utils.composable.PlatformClickableIconButton
import dev.toastbits.composekit.components.utils.composable.pane.model.InitialPaneRatioSource
import dev.toastbits.composekit.navigation.screen.ResponsiveTwoPaneScreen
import dev.toastbits.composekit.util.copy
import dev.toastbits.lifelog.application.core.FullContentScreen
import dev.toastbits.lifelog.application.core.usercontent.UserContentDisplay
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.VerticalLogTimeline
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.VerticalLogTimelineState
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.UserContent

class TopLogViewScreen(
    private val logDatabase: LogDatabase
): ResponsiveTwoPaneScreen<LogEventReference>(
    initialStartPaneRatioSource =
        InitialPaneRatioSource.Remembered(
            "logview.data.ui.screen.TopLogViewScreen",
            InitialPaneRatioSource.Ratio(0.3f)
        ),
    alwaysShowEndPane = true
), FullContentScreen {
    private var timelineState: VerticalLogTimelineState = VerticalLogTimelineState()
    private var viewingEvent: LogEventReference? by mutableStateOf(null)

    @Composable
    override fun getCurrentData(): LogEventReference? = viewingEvent

    @Composable
    override fun PrimaryPane(data: LogEventReference?, contentPadding: PaddingValues, modifier: Modifier) {
        var currentDateIndex: Int? by remember { mutableStateOf(null) }
        var scrollTargetDateIndex: Int? by remember { mutableStateOf(null) }

        val currentTimelineState: VerticalLogTimelineState =
            remember {
                VerticalLogTimelineState(timelineState).also { timelineState = it }
            }

        Column(
            modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VerticalLogTimeline(
                currentTimelineState,
                logDatabase,
                Modifier.fillMaxHeight().weight(1f),
                contentPadding = contentPadding.copy(bottom = 0.dp),
                scrollTargetDateIndex = scrollTargetDateIndex,
                onCurrentDateIndexChanged = {
                    currentDateIndex = it
                    scrollTargetDateIndex = null
                }
            ) { event ->
                viewingEvent = event
            }

            Row(Modifier.padding(contentPadding.copy(top = 0.dp))) {
                PlatformClickableIconButton(
                    onClick = {
                        val current: Int = currentDateIndex ?: return@PlatformClickableIconButton
                        scrollTargetDateIndex = current - 1
                    },
                    onAltClick = {
                        scrollTargetDateIndex = Int.MIN_VALUE
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, null) // TODO
                }
                PlatformClickableIconButton(
                    onClick = {
                        val current: Int = currentDateIndex ?: return@PlatformClickableIconButton
                        scrollTargetDateIndex = current + 1
                    },
                    onAltClick = {
                        scrollTargetDateIndex = Int.MAX_VALUE
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, null) // TODO
                }
            }
        }
    }

    @Composable
    override fun SecondaryPane(data: LogEventReference?, contentPadding: PaddingValues, modifier: Modifier) {
        if (data == null) {
            return
        }

        BackHandler(!isDisplayingBothPanes) {
            viewingEvent = null
        }

        ScrollBarLazyColumn(modifier, contentPadding = contentPadding) {
            item {
                Text("Secondary $data")

                val content: UserContent? = logDatabase[data].content
                if (content == null) {
                    Text("No content")
                }
                else {
                    UserContentDisplay(content)
                }
            }
        }
    }
}
