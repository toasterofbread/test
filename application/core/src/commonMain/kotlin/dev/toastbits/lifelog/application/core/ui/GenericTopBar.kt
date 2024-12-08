package dev.toastbits.lifelog.application.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import dev.toastbits.composekit.components.utils.composable.animatedvisibility.NullableValueAnimatedVisibility
import dev.toastbits.lifelog.application.core.generated.resources.Res
import dev.toastbits.lifelog.application.core.generated.resources.button_navigate_back
import org.jetbrains.compose.resources.stringResource

@Composable
fun GenericTopBar(
    title: String?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    backEnabled: Boolean = true,
    extraContent: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(onBack != null) {
            IconButton(
                { onBack?.invoke() },
                enabled = backEnabled
            ) {
                Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, stringResource(Res.string.button_navigate_back))
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            NullableValueAnimatedVisibility(
                title,
                Modifier.clipToBounds(),
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) { currentTitle ->
                if (currentTitle != null) {
                    Text(
                        currentTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
        }

        extraContent()
    }
}
