package dev.toastbits.lifelog.application.logview.data.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.platform.composable.BackHandler
import dev.toastbits.composekit.components.utils.composable.PlatformClickableIconButton
import dev.toastbits.composekit.components.utils.composable.pane.model.InitialPaneRatioSource
import dev.toastbits.composekit.components.utils.modifier.horizontal
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.screen.ResponsiveTwoPaneScreen
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.onAccent
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.composekit.util.copy
import dev.toastbits.lifelog.application.core.FullContentScreen
import dev.toastbits.lifelog.application.logview.data.ui.component.eventview.LogEventChanges
import dev.toastbits.lifelog.application.logview.data.ui.component.eventview.LogEventViewScreen
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.VerticalLogTimeline
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.VerticalLogTimelineState
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import lifelog.application.logview.data.generated.resources.Res
import lifelog.application.logview.data.generated.resources.`log_view_screen_changes_made_popup_$x`
import org.jetbrains.compose.resources.pluralStringResource

class TopLogViewScreen(
    private val logDatabase: LogDatabase
): ResponsiveTwoPaneScreen<LogEventViewScreen>(
    initialStartPaneRatioSource =
        InitialPaneRatioSource.Remembered(
            "logview.data.ui.screen.TopLogViewScreen",
            InitialPaneRatioSource.Ratio(0.3f)
        ),
    alwaysShowEndPane = true
), FullContentScreen {
    private var timelineState: VerticalLogTimelineState = VerticalLogTimelineState()
    private var viewingEventScreen: LogEventViewScreen? by mutableStateOf(null)
    private var showSearchBar: Boolean by mutableStateOf(false)

    private val eventChanges: MutableMap<LogEventReference, LogEventChanges> = mutableStateMapOf()

    @Composable
    override fun getCurrentData(): LogEventViewScreen? = viewingEventScreen

    @Composable
    override fun PrimaryPane(data: LogEventViewScreen?, contentPadding: PaddingValues, modifier: Modifier) {
        val density: Density = LocalDensity.current

        var currentDateIndex: Int? by remember { mutableStateOf(null) }
        var scrollTargetDateIndex: Int? by remember { mutableStateOf(null) }
        var shouldFocusSearchBar: Boolean by remember { mutableStateOf(false) }

        val currentTimelineState: VerticalLogTimelineState =
            remember {
                VerticalLogTimelineState(timelineState).also { timelineState = it }
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
                var bottomContentHeight: Dp by remember { mutableStateOf(0.dp) }

                VerticalLogTimeline(
                    currentTimelineState,
                    logDatabase,
                    Modifier.matchParentSize(),
                    contentPadding = contentPadding.copy(bottom = bottomContentHeight),
                    scrollTargetDateIndex = scrollTargetDateIndex,
                    onCurrentDateIndexChanged = {
                        currentDateIndex = it
                        scrollTargetDateIndex = null
                    }
                ) { eventReference ->
                    viewingEventScreen =
                        LogEventViewScreen(
                            eventReference,
                            logDatabase,
                            initialChanges = eventChanges[eventReference],
                            updateChanges = { newChanges ->
                                if (newChanges.hasChanges()) {
                                    eventChanges[eventReference] = newChanges
                                }
                                else {
                                    eventChanges.remove(eventReference)
                                }
                            }
                        )
                }

                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged {
                            with (density) {
                                bottomContentHeight = it.height.toDp()
                            }
                        }
                ) {
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

                    androidx.compose.animation.AnimatedVisibility(
                        showSearchBar,
                        enter = slideInVertically { it / 2 } + fadeIn(),
                        exit = slideOutVertically { it / 2 } + fadeOut()
                    ) {
                        SearchField(
                            shouldFocusSearchBar,
                            onClose = {
                                showSearchBar = false
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

            BottomBar(
                canScrollUp = currentTimelineState.columnState.canScrollBackward,
                canScrollDown = currentTimelineState.columnState.canScrollForward,
                searching = showSearchBar,
                currentDateIndex = currentDateIndex,
                setScrollTargetDateIndex = {
                    scrollTargetDateIndex = it
                },
                showSearchBar = {
                    showSearchBar = true
                    shouldFocusSearchBar = true
                },
                modifier = Modifier.padding(contentPadding.copy(top = 0.dp))
            )
        }
    }

    @Composable
    private fun ChangesBar(modifier: Modifier = Modifier) {
        val theme: ThemeValues = LocalComposeKitTheme.current
        Surface(
            onClick = {
                println("AAA")
            },
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

            Text(
                pluralStringResource(Res.plurals.`log_view_screen_changes_made_popup_$x`, changeCount)
                    .replace("\$x", changeCount.toString()),
                Modifier.padding(10.dp),
                color = theme.onAccent
            )
        }
    }

    @Composable
    private fun SearchField(
        focusImmediately: Boolean,
        onClose: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val focusRequester: FocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            if (focusImmediately) {
                focusRequester.requestFocus()
            }
        }

        TextField(
            timelineState.filterText.orEmpty(),
            onValueChange = {
                timelineState.filterText = it
            },
            modifier = modifier.focusRequester(focusRequester),
            trailingIcon = {
                Row {
                    IconButton({ timelineState.filterText = null }) {
                        Icon(Icons.Default.Close, null) // TODO
                    }
                    IconButton(onClose) {
                        Icon(Icons.Default.KeyboardArrowDown, null) // TODO
                    }
                }
            }
        )
    }

    @Composable
    private fun BottomBar(
        canScrollUp: Boolean,
        canScrollDown: Boolean,
        searching: Boolean,
        currentDateIndex: Int?,
        setScrollTargetDateIndex: (Int) -> Unit,
        showSearchBar: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Spacer(Modifier.fillMaxWidth().weight(1f))

            AnimatedVisibility(
                !searching,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                StyledButton(
                    onClick = showSearchBar
                ) {
                    Icon(Icons.Default.Search, null) // TODO
                }
            }

            StyledButton(
                onClick = {
                    val current: Int = currentDateIndex ?: return@StyledButton
                    setScrollTargetDateIndex(current - 1)
                },
                onAltClick = {
                    setScrollTargetDateIndex(Int.MIN_VALUE)
                },
                enabled = canScrollUp
            ) {
                Icon(Icons.Default.KeyboardArrowUp, null) // TODO
            }

            StyledButton(
                onClick = {
                    val current: Int = currentDateIndex ?: return@StyledButton
                    setScrollTargetDateIndex(current + 1)
                },
                onAltClick = {
                    setScrollTargetDateIndex(Int.MAX_VALUE)
                },
                enabled = canScrollDown
            ) {
                Icon(Icons.Default.KeyboardArrowDown, null) // TODO
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun StyledButton(
        onClick: () -> Unit,
        onAltClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        filled: Boolean = false,
        content: @Composable () -> Unit
    ) {
        val theme: ThemeValues = LocalComposeKitTheme.current
        val accent: Color by animateColorAsState(if (enabled) theme.accent else theme.accent.copy(alpha = 0.5f))

        PlatformClickableIconButton(
            onClick = onClick,
            onAltClick = onAltClick,
            modifier =
                modifier
                    .run {
                        if (filled) background(accent, CircleShape)
                        else border(2.dp, accent, CircleShape)
                    }
                    .size(IconButtonDefaults.smallContainerSize()),
            enabled = enabled
        ) {
            CompositionLocalProvider(
                LocalContentColor provides (
                    if (filled) theme.onAccent
                    else LocalContentColor.current
                ).copy(alpha = accent.alpha)
            ) {
                content()
            }
        }
    }

    @Composable
    override fun SecondaryPane(data: LogEventViewScreen?, contentPadding: PaddingValues, modifier: Modifier) {
        if (data == null) {
            return
        }

        BackHandler(!isDisplayingBothPanes) {
            viewingEventScreen = null
        }

        data.Content(LocalNavigator.current, modifier, contentPadding)
    }
}
