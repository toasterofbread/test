package dev.toastbits.lifelog.application.logview.component.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.utils.composable.PlatformClickableIconButton
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.onAccent
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme

@Composable
internal fun LogTimelineNavigationBar(
    canScrollUp: Boolean,
    canScrollDown: Boolean,
    searching: Boolean,
    scrollDateBy: (Int) -> Unit,
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
            onClick = { scrollDateBy(-1) },
            onAltClick = { scrollDateBy(Int.MIN_VALUE) },
            enabled = canScrollUp
        ) {
            Icon(Icons.Default.KeyboardArrowUp, null) // TODO
        }

        StyledButton(
            onClick = { scrollDateBy(1) },
            onAltClick = { scrollDateBy(Int.MAX_VALUE) },
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
