package dev.toastbits.lifelog.application.logview.data.ui.screen

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
import dev.toastbits.composekit.components.utils.composable.pane.model.InitialPaneRatioSource
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.ResponsiveTwoPaneScreen
import dev.toastbits.composekit.util.bottom
import dev.toastbits.composekit.util.copy
import dev.toastbits.composekit.util.top
import dev.toastbits.lifelog.application.core.FullContentScreen
import dev.toastbits.lifelog.application.core.ui.GenericTopBar
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.DefaultLogTimelineColumn
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.model.LogTimelineState
import dev.toastbits.lifelog.application.logview.data.ui.model.LogEventChanges
import dev.toastbits.lifelog.application.logview.data.ui.model.LogEventReference
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import lifelog.application.logview.data.generated.resources.Res
import lifelog.application.logview.data.generated.resources.log_view_screen_title_review_changes
import org.jetbrains.compose.resources.stringResource

class LogListChangesScreen(
    private val logDatabase: LogDatabase,
    eventChanges: Map<LogEventReference, LogEventChanges>,
    private val discardChanges: (LogEventReference) -> Unit
): ResponsiveTwoPaneScreen<LogEventChangesScreen>(
    initialStartPaneRatioSource =
    InitialPaneRatioSource.Remembered(
        "logview.data.ui.screen.LogListSaveScreen",
        InitialPaneRatioSource.Ratio(0.3f)
    ),
    alwaysShowEndPane = true
), FullContentScreen {
    private var eventChanges: Map<LogEventReference, LogEventChanges> by mutableStateOf(eventChanges)
    private var timelineState: LogTimelineState = LogTimelineState()
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
                LogTimelineState(timelineState).also { timelineState = it }
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

        data.Content(LocalNavigator.current, modifier, contentPadding)
    }
}
