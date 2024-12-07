package dev.toastbits.lifelog.application.logview.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import dev.toastbits.composekit.components.platform.composable.BackHandler
import dev.toastbits.composekit.components.platform.composable.ScrollBarLazyColumn
import dev.toastbits.composekit.components.platform.composable.ScrollBarLazyRow
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.composekit.util.plus
import dev.toastbits.lifelog.application.logview.component.event.LogEventMetadata
import dev.toastbits.lifelog.application.logview.component.event.LogEventUserContent
import dev.toastbits.lifelog.application.logview.component.propertychip.withProperties
import dev.toastbits.lifelog.application.logview.component.timeline.WaveBorder
import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.application.logview.model.LogEventViewScreenState
import dev.toastbits.lifelog.application.logview.model.awaitLoaded
import dev.toastbits.lifelog.application.logview.model.getNext
import dev.toastbits.lifelog.core.specification.converter.generateUserContent
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val CHANGES_UPDATE_DELAY: Duration = 500.milliseconds

class LogEventScreen<T: LogEvent>(
    private val event: T,
    private val date: LogDate,
    private val logDatabase: LogDatabase,
    initialChanges: LogEntityChanges<T>,
    private val onChangesChanged: (LogEntityChanges<T>) -> Unit
): Screen {
    private val coroutineScope: CoroutineScope = CoroutineScope(Job())

    private var changes: LogEntityChanges<T> by mutableStateOf(initialChanges)
    private var state: LogEventViewScreenState by mutableStateOf(LogEventViewScreenState.Loaded.Preview(getCurrentContent()))

    override fun onClosed() {
        super.onClosed()
        coroutineScope.cancel()
    }

    fun updateChanges(changes: LogEntityChanges<T>?) {
        this.changes = changes ?: LogEntityChanges.createEmpty()

        coroutineScope.coroutineContext.cancelChildren()
        state =
            when (state.type) {
                LogEventViewScreenState.Type.EDIT ->
                    LogEventViewScreenState.Loading.of(
                        coroutineScope.async {
                            LogEventViewScreenState.Loaded.Edit(
                                logDatabase.converter.generateUserContent(getCurrentContent(), date)
                            )
                        }
                    )
                LogEventViewScreenState.Type.PREVIEW ->
                    LogEventViewScreenState.Loaded.Preview(getCurrentContent())
            }
    }

    private fun onChange(change: LogEntityChanges.Change<T, *>) {
        updateChanges(changes.copyWithChange(change))
    }

    private fun getCurrentContent(): UserContent =
        changes.firstWithPropertyOrNull(LogEvent.PROPERTY_CONTENT)?.newValue ?: event.content ?: UserContent.EMPTY

    private fun openNextState() {
        coroutineScope.coroutineContext.cancelChildren()
        state = state.getNext(date, logDatabase.converter, coroutineScope)
    }

    @Composable
    override fun Content(navigator: Navigator, modifier: Modifier, contentPadding: PaddingValues) {
        val scrollBarSpacing: Dp = 5.dp
        val scrollBarThickness: Dp = 8.dp
        val density: Density = LocalDensity.current

        var bottomContentHeight: Dp by remember { mutableStateOf(0.dp) }

        BackHandler(state.type == LogEventViewScreenState.Type.EDIT) {
            openNextState()
        }

        LaunchedEffect(state) {
            val state: LogEventViewScreenState = state
            if (state.type == LogEventViewScreenState.Type.EDIT) {
                delay(CHANGES_UPDATE_DELAY)
            }

            val loadedState: LogEventViewScreenState.Loaded = state.awaitLoaded()

            val newContent: UserContent =
                when (loadedState) {
                    is LogEventViewScreenState.Loaded.Edit ->
                        logDatabase.converter.userContentParser.parseUserContent(
                            loadedState.content,
                            logDatabase.converter.referenceParser,
                            onAlert = { _, _ -> }
                        )
                    is LogEventViewScreenState.Loaded.Preview -> loadedState.content
                }

            changes =
                changes.copyWithProperty(
                    LogEvent.PROPERTY_CONTENT, newContent
                )
            onChangesChanged(changes)
        }

        Box(modifier) {
            CompositionLocalProvider(
                LocalBringIntoViewSpec provides object : BringIntoViewSpec {
                    override fun calculateScrollDistance(
                        offset: Float,
                        size: Float,
                        containerSize: Float
                    ): Float = 0f
                }
            ) {
                ScrollBarLazyColumn(
                    contentPadding = contentPadding + PaddingValues(bottom = bottomContentHeight),
                    scrollBarContentPadding = contentPadding,
                    scrollBarSpacing = scrollBarSpacing,
                    scrollBarThickness = scrollBarThickness
                ) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            val modifiedEvent: T =
                                remember(changes, event) {
                                    changes.applyTo(event)
                                }

                            LogEventMetadata(modifiedEvent, date, logDatabase.configuration)

                            modifiedEvent.withProperties(logDatabase.configuration) {
                                ScrollBarLazyRow(
                                    Modifier.height(58.dp)
                                ) {
                                    item {
                                        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                                            for (propertyIndex in 0 until propertyCount) {
                                                if (shouldPropertyShow(propertyIndex)) {
                                                    PropertyChip(
                                                        propertyIndex,
                                                        onEdit =
                                                            if (state.type == LogEventViewScreenState.Type.EDIT) ::onChange
                                                            else null,
                                                        Modifier.fillMaxHeight()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            val content: UserContent? = event.content
                            if (content != null) {
                                LogEventUserContent(state) {
                                    state = it
                                }
                            }
                            else {
                                Text("No content // TODO")
                            }
                        }
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(contentPadding)
                    .padding(end = scrollBarSpacing + scrollBarThickness)
                    .onSizeChanged {
                        with (density) {
                            bottomContentHeight = it.height.toDp()
                        }
                    },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    state.type == LogEventViewScreenState.Type.EDIT,
                    Modifier.fillMaxWidth().weight(1f),
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    WaveBorder(
                        Modifier.fillMaxWidth(),
                        waveThickness = 5.dp
                    )
                }

                StateCycleButton({ openNextState() })
            }
        }
    }

    @Composable
    private fun StateCycleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
        FilledIconButton(onClick, modifier) {
            Crossfade(state.type) {
                when (it) {
                    LogEventViewScreenState.Type.EDIT -> Icon(Icons.Default.Visibility, null) // TODO
                    LogEventViewScreenState.Type.PREVIEW -> Icon(Icons.Default.Edit, null) // TODO
                }
            }
        }
    }
}
