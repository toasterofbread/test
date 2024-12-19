package dev.toastbits.lifelog.application.logview.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.ui.component.GenericTopBar
import dev.toastbits.composekit.components.utils.composable.pane.model.InitialPaneRatioSource
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.ResponsiveTwoPaneScreen
import dev.toastbits.composekit.util.composable.bottom
import dev.toastbits.composekit.util.composable.copy
import dev.toastbits.composekit.util.composable.top
import dev.toastbits.lifelog.application.core.FullContentScreen
import dev.toastbits.lifelog.application.logview.component.timeline.DefaultLogTimelineColumn
import dev.toastbits.lifelog.application.logview.component.timeline.model.LogTimelineState
import dev.toastbits.lifelog.application.logview.generated.resources.Res
import dev.toastbits.lifelog.application.logview.generated.resources.log_view_screen_title_review_changes
import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.application.logview.model.LogEventReference
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import org.jetbrains.compose.resources.stringResource

class LogListChangesScreen(
    private val logDatabase: LogDatabase,
    eventChanges: Map<LogEventReference, LogEntityChanges<LogEvent>>,
    private val discardChanges: (LogEventReference) -> Unit
): ResponsiveTwoPaneScreen<LogEventChangesScreen>(
    initialStartPaneRatioSource =
        InitialPaneRatioSource.Remembered(
            "logview.screen.LogListSaveScreen",
            InitialPaneRatioSource.Ratio(0.3f)
        ),
    alwaysShowEndPane = true
), FullContentScreen {
    private var eventChanges: Map<LogEventReference, LogEntityChanges<LogEvent>> by mutableStateOf(eventChanges)
    private var timelineState: LogTimelineState? = null
    private var viewingEventScreen: LogEventChangesScreen? by mutableStateOf(
        eventChanges.entries.firstOrNull()?.let { (eventReference, eventChanges) ->
            LogEventChangesScreen(
                eventReference,
                eventChanges,
                logDatabase,
                onDiscardChanges = ::discardEventChanges
            )
        }
    )
    private var showSearchBar: Boolean by mutableStateOf(false)

    private fun discardEventChanges() {
        val reference: LogEventReference = viewingEventScreen?.eventReference ?: return
        eventChanges = eventChanges.toMutableMap().apply { remove(reference) }
        viewingEventScreen = null
        discardChanges(reference)
    }

    @Composable
    override fun getCurrentData(): LogEventChangesScreen? = viewingEventScreen

    @Composable
    override fun PrimaryPane(data: LogEventChangesScreen?, contentPadding: PaddingValues, modifier: Modifier) {
        val navigator: Navigator = LocalNavigator.current
        val currentTimelineState: LogTimelineState =
            remember {
                LogTimelineState(from = timelineState).also { timelineState = it }
            }

        Column(
            modifier.padding(
                contentPadding.copy(
                    top = if (isDisplayingBothPanes) 0.dp else contentPadding.top,
                    bottom = 0.dp
                )
            )
        ) {
            if (!isDisplayingBothPanes) {
                GenericTopBar(
                    title = stringResource(Res.string.log_view_screen_title_review_changes),
                    onBack = { navigator.navigateBackward() }
                )
            }

            DefaultLogTimelineColumn(
                contentPadding =
                    PaddingValues(
                        top = if (isDisplayingBothPanes) contentPadding.top else 0.dp,
                        bottom = contentPadding.bottom
                    ),
                timelineState = currentTimelineState,
                logDatabase = logDatabase,
                isEventSelected = {
                    viewingEventScreen?.eventReference == it
                },
                showSearchBar = showSearchBar,
                setShowSearchBar = { showSearchBar = it },
                onEventSelected = { eventReference ->
                    viewingEventScreen =
                        LogEventChangesScreen(
                            eventReference,
                            eventChanges[eventReference]!!,
                            logDatabase,
                            onDiscardChanges = ::discardEventChanges
                        )
                },
                filterEvents = { _, reference ->
                    eventChanges.contains(reference)
                },
                filterKey = eventChanges
            )
        }
    }

    @Composable
    override fun SecondaryPane(
        data: LogEventChangesScreen?,
        contentPadding: PaddingValues,
        modifier: Modifier
    ) {
        if (data == null) {
            return
        }

        data.Content(modifier, contentPadding)
    }
}
