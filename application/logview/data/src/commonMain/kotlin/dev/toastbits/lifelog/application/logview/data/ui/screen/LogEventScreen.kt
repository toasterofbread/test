package dev.toastbits.lifelog.application.logview.data.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.composekit.util.launchSingle
import dev.toastbits.composekit.util.plus
import dev.toastbits.lifelog.application.logview.data.ui.component.event.LogEventMetadata
import dev.toastbits.lifelog.application.logview.data.ui.component.event.LogEventUserContent
import dev.toastbits.lifelog.application.logview.data.ui.model.LogEventChanges
import dev.toastbits.lifelog.application.logview.data.ui.model.LogEventReference
import dev.toastbits.lifelog.application.logview.data.ui.model.LogEventViewScreenState
import dev.toastbits.lifelog.application.logview.data.ui.model.get
import dev.toastbits.lifelog.application.logview.data.ui.model.getNext
import dev.toastbits.lifelog.application.logview.data.ui.model.getNextType
import dev.toastbits.lifelog.core.specification.converter.generateUserContent
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val CHANGES_UPDATE_DELAY: Duration = 500.milliseconds

class LogEventScreen(
    val eventReference: LogEventReference,
    private val logDatabase: LogDatabase,
    private var changes: LogEventChanges,
    private val updateChanges: (LogEventChanges) -> Unit
): Screen {
    private val coroutineScope: CoroutineScope = CoroutineScope(Job())
    private val event: LogEvent = logDatabase[eventReference]

    private var state: LogEventViewScreenState by mutableStateOf(
        LogEventViewScreenState.Previewing(getCurrentContent())
    )
    private var loadingNextStateType: LogEventViewScreenState.Type? by mutableStateOf(null)

    override fun onClosed() {
        super.onClosed()
        coroutineScope.cancel()
    }

    fun setChanges(changes: LogEventChanges?) {
        this.changes = changes ?: LogEventChanges.EMPTY

        coroutineScope.launchSingle {
            loadingNextStateType = state.type
            state =
                when (state) {
                    is LogEventViewScreenState.Editing ->
                        LogEventViewScreenState.Editing(
                            logDatabase.converter.generateUserContent(getCurrentContent(), eventReference.date)
                        )
                    is LogEventViewScreenState.Previewing ->
                        LogEventViewScreenState.Previewing(getCurrentContent())
                }
            loadingNextStateType = null
        }
    }

    private fun getCurrentContent(): UserContent =
        changes.content ?: event.content ?: UserContent.EMPTY

    private fun openNextState() {
        coroutineScope.launchSingle {
            if (loadingNextStateType != null) {
                loadingNextStateType = null
                return@launchSingle
            }
            loadingNextStateType = state.getNextType()
            state = state.getNext(eventReference, logDatabase.converter)
            loadingNextStateType = null
        }
    }

    @Composable
    override fun Content(navigator: Navigator, modifier: Modifier, contentPadding: PaddingValues) {
        val scrollBarSpacing: Dp = 5.dp
        val scrollBarThickness: Dp = 8.dp
        val density: Density = LocalDensity.current

        val event: LogEvent = remember(eventReference) { logDatabase[eventReference] }
        var bottomContentHeight: Dp by remember { mutableStateOf(0.dp) }

        BackHandler((loadingNextStateType ?: state.type) == LogEventViewScreenState.Type.EDIT) {
            if (loadingNextStateType != null) {
                loadingNextStateType = null
            }
            else {
                openNextState()
            }
        }

        LaunchedEffect(state) {
            val state: LogEventViewScreenState = state
            if (state is LogEventViewScreenState.Editing) {
                delay(CHANGES_UPDATE_DELAY)
            }

            val newContent: UserContent =
                when (state) {
                    is LogEventViewScreenState.Editing ->
                        logDatabase.converter.userContentParser.parseUserContent(
                            state.content,
                            logDatabase.converter.referenceParser,
                            onAlert = { _, _ -> }
                        )
                    is LogEventViewScreenState.Previewing -> state.content
                }

            changes = changes.copy(
                content =
                    if (newContent == (event.content ?: UserContent.EMPTY)) null
                    else newContent
            )
            updateChanges(changes)
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
                            LogEventMetadata(event)

                            val content: UserContent? = event.content
                            if (content != null) {
                                LogEventUserContent(
                                    state,
                                    loadingNextStateType
                                ) {
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
                horizontalArrangement = Arrangement.End
            ) {
                EditToggleButton({ openNextState() })
            }
        }
    }

    @Composable
    private fun EditToggleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
        FilledIconButton(onClick, modifier) {
            Crossfade(loadingNextStateType ?: state.type) {
                when (it) {
                    LogEventViewScreenState.Type.EDIT -> Icon(Icons.Default.Visibility, null) // TODO
                    LogEventViewScreenState.Type.PREVIEW -> Icon(Icons.Default.Edit, null) // TODO
                }
            }
        }
    }
}
