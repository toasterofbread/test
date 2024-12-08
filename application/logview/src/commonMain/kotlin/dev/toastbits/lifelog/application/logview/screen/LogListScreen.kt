package dev.toastbits.lifelog.application.logview.screen

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.platform.composable.BackHandler
import dev.toastbits.composekit.components.utils.composable.pane.model.InitialPaneRatioSource
import dev.toastbits.composekit.components.utils.modifier.horizontal
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.ResponsiveTwoPaneScreen
import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.onAccent
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.lifelog.application.core.FullContentScreen
import dev.toastbits.lifelog.application.logview.component.timeline.DefaultLogTimelineColumn
import dev.toastbits.lifelog.application.logview.component.timeline.model.LogTimelineState
import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.application.logview.model.LogEventReference
import dev.toastbits.lifelog.application.logview.model.get
import dev.toastbits.lifelog.application.logview.model.getOrNull
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.application.logview.generated.resources.Res
import dev.toastbits.lifelog.application.logview.generated.resources.`log_view_screen_$x_changes_made_popup`
import dev.toastbits.lifelog.application.logview.generated.resources.log_view_screen_button_review_changes
import dev.toastbits.lifelog.application.logview.generated.resources.log_view_screen_button_save
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

class LogListScreen(
    initialLogDatabase: LogDatabase,
    private val logSaveScreenProvider: LogSaveScreenProvider
): ResponsiveTwoPaneScreen<LogEventScreen<LogEvent>>(
    initialStartPaneRatioSource =
        InitialPaneRatioSource.Remembered(
            "logview.screen.LogListScreen",
            InitialPaneRatioSource.Ratio(0.3f)
        ),
    alwaysShowEndPane = true
), FullContentScreen {
    data class EventScreen(
        val eventReference: LogEventReference,
        val screen: LogEventScreen<LogEvent>
    )

    private var timelineState: LogTimelineState? = null
    private var logDatabase: LogDatabase by mutableStateOf(initialLogDatabase)
    private var viewingEventScreen: EventScreen? by mutableStateOf(null)
    private var showSearchBar: Boolean by mutableStateOf(false)

    private val eventChanges: MutableMap<LogEventReference, LogEntityChanges<LogEvent>> = mutableStateMapOf()

    @Composable
    override fun getCurrentData(): LogEventScreen<LogEvent>? = viewingEventScreen?.screen

    @Composable
    override fun PrimaryPane(data: LogEventScreen<LogEvent>?, contentPadding: PaddingValues, modifier: Modifier) {
        val currentTimelineState: LogTimelineState =
            remember {
                LogTimelineState(
                    firstVisibleItemIndex = Int.MAX_VALUE,
                    from = timelineState
                ).also { timelineState = it }
            }

        DefaultLogTimelineColumn(
            contentPadding = contentPadding,
            timelineState = currentTimelineState,
            logDatabase = logDatabase,
            isEventSelected = {
                viewingEventScreen?.eventReference == it
            },
            showSearchBar = showSearchBar,
            setShowSearchBar = { showSearchBar = it },
            modifier = modifier,
            onEventSelected = { eventReference ->
                viewingEventScreen =
                    EventScreen(
                        eventReference,
                        LogEventScreen(
                            event = logDatabase[eventReference],
                            date = eventReference.date,
                            logDatabase = logDatabase,
                            initialChanges =
                                eventChanges[eventReference]
                                ?: LogEntityChanges.createEmpty(),
                            onChangesChanged = { event: LogEvent, newChanges: LogEntityChanges<LogEvent> ->
                                if (newChanges.hasChanges(event)) {
                                    eventChanges[eventReference] = newChanges
                                }
                                else {
                                    eventChanges.remove(eventReference)
                                }
                            }
                        )
                    )
            },
            extraFloatingContent = {
                androidx.compose.animation.AnimatedVisibility(
                    eventChanges.isNotEmpty(),
                    enter = slideInVertically { it / 2 } + fadeIn(),
                    exit = slideOutVertically { it / 2 } + fadeOut()
                ) {
                    ChangesBar(
                        Modifier
                            .fillMaxWidth()
                            .padding(contentPadding.horizontal)
                    )
                }
            }
        )
    }

    @Composable
    private fun ChangesBar(modifier: Modifier = Modifier) {
        val theme: ThemeValues = LocalComposeKitTheme.current
        val navigator: Navigator = LocalNavigator.current

        Surface(
            modifier = modifier.padding(15.dp),
            shape = MaterialTheme.shapes.small,
            color = theme.accent,
            contentColor = theme.onAccent
        ) {
            var changeCount: Int by remember { mutableStateOf(eventChanges.size.coerceAtLeast(1)) }
            LaunchedEffect(eventChanges.size) {
                if (eventChanges.isNotEmpty()) {
                    changeCount = eventChanges.size
                }
            }

            FlowRow(
                Modifier.padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.End,
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    pluralStringResource(Res.plurals.`log_view_screen_$x_changes_made_popup`, changeCount)
                        .replace("\$x", changeCount.toString()),
                    Modifier.fillMaxWidth().weight(1f),
                    color = theme.onAccent
                )

                Row {
                    IconButton({
                        val changes: Map<LogEventReference, LogEntityChanges<LogEvent>> = eventChanges.toMap()
                        if (changes.isNotEmpty()) {
                            navigator.pushScreen(
                                LogListChangesScreen(
                                    logDatabase,
                                    changes,
                                    discardChanges = { eventReference ->
                                        eventChanges.remove(eventReference)
                                        if (viewingEventScreen?.eventReference == eventReference) {
                                            viewingEventScreen?.screen?.updateChanges(null)
                                        }
                                    }
                                )
                            )
                        }
                    }) {
                        Icon(Icons.Default.Visibility, stringResource(Res.string.log_view_screen_button_review_changes))
                    }

                    IconButton({ saveChanges(navigator) }) {
                        Icon(Icons.Default.Save, stringResource(Res.string.log_view_screen_button_save))
                    }
                }
            }
        }
    }

    @Composable
    override fun SecondaryPane(data: LogEventScreen<LogEvent>?, contentPadding: PaddingValues, modifier: Modifier) {
        if (data == null) {
            return
        }

        BackHandler(!isDisplayingBothPanes) {
            viewingEventScreen = null
        }

        data.Content(LocalNavigator.current, modifier.fillMaxSize(), contentPadding)
    }

    private fun applyChangesToDatabase(): LogDatabase? {
        if (eventChanges.isEmpty()) {
            return null
        }

        return logDatabase.copy(
            days = logDatabase.days.toMutableMap().also { days ->
                for ((ref, changes) in eventChanges) {
                    val events: List<LogEvent> = days[ref.date]!!
                    days[ref.date] = events.toMutableList().apply {
                        set(ref.logIndex, changes.applyTo(get(ref.logIndex)))
                    }
                }
            }
        )
    }

    private fun saveChanges(navigator: Navigator) {
        val newDatabase: LogDatabase =
            applyChangesToDatabase() ?: return

        val saveScreen: Screen =
            logSaveScreenProvider(
                database = newDatabase,
                autoProceed = false,
                onProceeded = null,
                onSaveFinished = { result ->
                    if (result.isSuccess) {
                        onDatabaseSaved(newDatabase)
                    }
                }
            )

        navigator.pushScreen(saveScreen)
    }

    private fun onDatabaseSaved(newDatabase: LogDatabase) {
        logDatabase = newDatabase
        eventChanges.clear()

        viewingEventScreen?.also {
            val newEvent: LogEvent? = logDatabase.getOrNull(it.eventReference)
            if (newEvent == null) {
                viewingEventScreen = null
            }
            else {
                it.screen.event = newEvent
                it.screen.updateChanges(null)
            }
        }
    }
}
