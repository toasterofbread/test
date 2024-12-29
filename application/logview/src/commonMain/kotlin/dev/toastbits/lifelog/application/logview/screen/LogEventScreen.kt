package dev.toastbits.lifelog.application.logview.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.platform.composable.ScrollBarColumn
import dev.toastbits.composekit.components.platform.composable.ScrollBarLazyRow
import dev.toastbits.composekit.components.utils.composable.SubtleLoadingIndicator
import dev.toastbits.composekit.components.utils.composable.wave.WaveBorder
import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.composekit.util.composable.bottom
import dev.toastbits.composekit.util.composable.plus
import dev.toastbits.composekit.util.composable.top
import dev.toastbits.composekit.util.platform.launchSingle
import dev.toastbits.lifelog.application.logview.component.event.LogEventMetadata
import dev.toastbits.lifelog.application.logview.component.event.LogEventUserContent
import dev.toastbits.lifelog.application.logview.component.propertychip.withProperties
import dev.toastbits.lifelog.application.logview.manager.LogDatabaseQueuedChanges
import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.application.logview.model.LogEventViewContentState
import dev.toastbits.lifelog.core.specification.converter.generateUserContent
import dev.toastbits.lifelog.core.specification.converter.parseUserContent
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.ensureActive
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val CHANGES_UPDATE_DELAY: Duration = 500.milliseconds

class LogEventScreen<T: LogEvent>(
    event: T,
    private val defaultEvent: T,
    private val date: LogDate,
    private val logDatabase: LogDatabase,
    initialChanges: LogEntityChanges<T>,
    private val onChangesChanged: (LogDatabaseQueuedChanges) -> Unit
): Screen {
    private val stateLoadCoroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    internal var event: T by mutableStateOf(event)
    private var changes: LogEntityChanges<T> by mutableStateOf(initialChanges)

    private var currentState: LogEventViewContentState by mutableStateOf(LogEventViewContentState.Preview(getCurrentContent()))

    private fun updateState(state: LogEventViewContentState) {
        currentState = state
        onChangesChanged(
            LogDatabaseQueuedChanges(CHANGES_UPDATE_DELAY) { changes ->
                val content: UserContent =
                    when (state) {
                        is LogEventViewContentState.Edit ->
                            logDatabase.converter.userContentParser.parseUserContent(
                                state.content,
                                logDatabase.converter.referenceParser,
                                onAlert = { _, _ -> }
                            )
                        is LogEventViewContentState.Preview -> state.content
                    }

                return@LogDatabaseQueuedChanges changes.copyWithProperty(LogEvent.PROPERTY_CONTENT, content)
            }
        )
    }

    private var stateLoadJob: Job? by mutableStateOf(null)
    private val loading: Boolean get() =
        stateLoadJob != null

    override fun release() {
        stateLoadCoroutineScope.cancel()
    }

    fun updateChanges(changes: LogEntityChanges<T>?) {
        val previousContent: UserContent = getCurrentContent()
        this.changes = changes ?: LogEntityChanges.createEmpty(defaultEvent)

        val currentContent: UserContent = getCurrentContent()
        if (previousContent == currentContent) {
            return
        }

        stateLoadCoroutineScope.coroutineContext.cancelChildren()
        updateState(
            when (currentState) {
                is LogEventViewContentState.Edit ->
                    LogEventViewContentState.Edit(logDatabase.converter.generateUserContent(currentContent, date))
                is LogEventViewContentState.Preview ->
                    LogEventViewContentState.Preview(currentContent)
            }
        )
    }

    private fun onChange(change: LogEntityChanges.Change<T, *>) {
        updateChanges(changes.copyWithChange(change))
    }

    private fun getCurrentContent(): UserContent =
        changes.firstWithPropertyOrNull(LogEvent.PROPERTY_CONTENT)?.newValue
            ?: event.content
            ?: UserContent.EMPTY

    @Composable
    override fun Content(modifier: Modifier, contentPadding: PaddingValues) {
        val scrollBarSpacing: Dp = 5.dp
        val scrollBarThickness: Dp = 8.dp
        val density: Density = LocalDensity.current

        var bottomContentHeight: Dp by remember { mutableStateOf(0.dp) }

        BoxWithConstraints(modifier) {
            CompositionLocalProvider(
                LocalBringIntoViewSpec provides object : BringIntoViewSpec {
                    override fun calculateScrollDistance(
                        offset: Float,
                        size: Float,
                        containerSize: Float
                    ): Float = 0f
                }
            ) {
                val scrollBarContentPadding: PaddingValues =
                    contentPadding + PaddingValues(bottom = bottomContentHeight)

                ScrollBarColumn(
                    contentPadding = scrollBarContentPadding,
                    scrollBarContentPadding = contentPadding,
                    scrollBarSpacing = scrollBarSpacing,
                    scrollBarThickness = scrollBarThickness,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val modifiedEvent: T =
                        remember(changes, event) {
                            changes.applyTo(event)
                        }

                    LogEventMetadata(modifiedEvent, date, logDatabase.configuration)

                    PropertiesRow(
                        modifiedEvent,
                        Modifier.height(58.dp),
                        listOf(LogEvent.PROPERTY_CONTENT)
                    )

                    var contentPosition: Dp by remember { mutableStateOf(0.dp) }
                    LogEventUserContent(
                        currentState.takeIf { !loading },
                        Modifier
                            .onGloballyPositioned {
                                with (density) {
                                    contentPosition = it.positionInParent().y.toDp()
                                }
                            }
                            .heightIn(
                                min =
                                    this@BoxWithConstraints.maxHeight
                                        - contentPosition
                                        - scrollBarContentPadding.top
                                        - scrollBarContentPadding.bottom
                            )

                    ) {
                        updateState(it)
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
                    currentState is LogEventViewContentState.Edit,
                    Modifier.fillMaxWidth().weight(1f),
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    WaveBorder(
                        Modifier.fillMaxWidth(),
                        waveThickness = 5.dp
                    )
                }

                EditToggleButton(
                    onClick = {
                        stateLoadJob?.also { job ->
                            job.cancel()
                            stateLoadJob = null
                            return@EditToggleButton
                        }

                        loadToggledState()
                    }
                )
            }
        }
    }

    private fun loadToggledState() {
        stateLoadJob = stateLoadCoroutineScope.launchSingle {
            val state: LogEventViewContentState = currentState
            updateState(
                when (state) {
                    is LogEventViewContentState.Edit ->
                        LogEventViewContentState.Preview(
                            logDatabase.converter.parseUserContent(state.content)
                        )
                    is LogEventViewContentState.Preview ->
                        LogEventViewContentState.Edit(
                            logDatabase.converter.generateUserContent(state.content, date)
                        )
                }
            )
            ensureActive()
            stateLoadJob = null
        }
    }

    @Composable
    private fun PropertiesRow(
        event: T,
        modifier: Modifier = Modifier,
        excludedProperties: List<LogEntityProperty<in T, *>> = emptyList()
    ) {
        event.withProperties(logDatabase.configuration) {
            ScrollBarLazyRow(modifier) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        for (property in properties) {
                            if (!property.shouldShow(event) || excludedProperties.contains(property)) {
                                continue
                            }

                            PropertyChip(
                                property,
                                onEdit =
                                    if (currentState is LogEventViewContentState.Edit) ::onChange
                                    else null,
                                Modifier.fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun EditToggleButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        FilledIconButton(onClick, modifier) {
            Crossfade(
                if (loading) null
                else currentState is LogEventViewContentState.Edit
            ) {
                when (it) {
                    null -> SubtleLoadingIndicator()
                    true -> Icon(Icons.Default.Visibility, null) // TODO
                    false -> Icon(Icons.Default.Edit, null) // TODO
                }
            }
        }
    }
}
