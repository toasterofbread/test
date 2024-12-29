package dev.toastbits.lifelog.application.logview.component.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.utils.composable.LoadActionIconButton
import dev.toastbits.composekit.components.utils.modifier.background
import dev.toastbits.composekit.components.utils.modifier.border
import dev.toastbits.composekit.theme.core.ThemeValues
import dev.toastbits.composekit.theme.core.onAccent
import dev.toastbits.composekit.theme.core.ui.LocalComposeKitTheme
import dev.toastbits.lifelog.application.logview.generated.resources.Res
import dev.toastbits.lifelog.application.logview.generated.resources.log_view_screen_button_add_event
import dev.toastbits.lifelog.application.logview.generated.resources.log_view_screen_button_scroll_next
import dev.toastbits.lifelog.application.logview.generated.resources.log_view_screen_button_scroll_previous
import dev.toastbits.lifelog.application.logview.generated.resources.log_view_screen_button_search
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LogTimelineNavigationBar(
    canScrollUp: Boolean,
    canScrollDown: Boolean,
    searching: Boolean,
    onAddEvent: (suspend () -> Unit)?,
    scrollDateBy: ((Int) -> Unit)?,
    showSearchBar: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        if (onAddEvent != null) {
            StyledButton(
                onClick = onAddEvent
            ) {
                Icon(Icons.Default.Add, stringResource(Res.string.log_view_screen_button_add_event))
            }
        }

        Spacer(Modifier.fillMaxWidth().weight(1f))

        if (showSearchBar != null) {
            AnimatedVisibility(
                !searching,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                StyledButton(
                    onClick = showSearchBar
                ) {
                    Icon(Icons.Default.Search, stringResource(Res.string.log_view_screen_button_search))
                }
            }
        }

        if (scrollDateBy != null) {
            StyledButton(
                onClick = { scrollDateBy(-1) },
                onAltClick = { scrollDateBy(Int.MIN_VALUE) },
                enabled = canScrollUp
            ) {
                Icon(Icons.Default.KeyboardArrowUp, stringResource(Res.string.log_view_screen_button_scroll_previous))
            }

            StyledButton(
                onClick = { scrollDateBy(1) },
                onAltClick = { scrollDateBy(Int.MAX_VALUE) },
                enabled = canScrollDown
            ) {
                Icon(Icons.Default.KeyboardArrowDown, stringResource(Res.string.log_view_screen_button_scroll_next))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StyledButton(
    onClick: suspend () -> Unit,
    onAltClick: (suspend () -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val theme: ThemeValues = LocalComposeKitTheme.current
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val hovered: Boolean by interactionSource.collectIsHoveredAsState()

    val backgroundColour: Color by animateColorAsState(if (hovered) theme.accent else theme.background)
    val contentColour: Color by animateColorAsState(
        (if (hovered) theme.onAccent else theme.onBackground)
            .copy(alpha = if (enabled) 1f else 0.5f)
    )
    val borderColour: Color by animateColorAsState(
        theme.accent.copy(alpha = if (enabled) 1f else 0.5f)
    )

    LoadActionIconButton(
        performLoad = {
            if (it) {
                onAltClick?.invoke()
            }
            else {
                onClick()
            }
        },
        hasAltLoadAction = onAltClick != null,
        modifier =
            modifier
                .hoverable(interactionSource)
                .background(CircleShape) { backgroundColour }
                .border(2.dp, CircleShape) { borderColour }
                .size(IconButtonDefaults.smallContainerSize()),
        enabled = enabled
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColour
        ) {
            content()
        }
    }
}
