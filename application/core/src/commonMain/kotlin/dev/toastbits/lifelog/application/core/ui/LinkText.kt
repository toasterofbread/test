package dev.toastbits.lifelog.application.core.ui

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import dev.toastbits.composekit.components.LocalContext
import dev.toastbits.composekit.components.platform.composable.platformClickable
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.composekit.theme.core.ui.LocalComposeKitTheme
import dev.toastbits.composekit.theme.core.vibrantAccent
import dev.toastbits.composekit.util.thenWith

@Composable
fun LinkText(
    text: String,
    url: String?,
    modifier: Modifier = Modifier,
    linkContentDescription: String? = null,
    style: TextStyle = LocalTextStyle.current,
    softWrap: Boolean = true
) {
    val context: PlatformContext = LocalContext.current

    Text(
        text,
        style = style,
        color =
            if (url != null) LocalComposeKitTheme.current.vibrantAccent
            else Color.Unspecified,
        softWrap = softWrap,
        modifier =
            modifier.thenWith(
                url?.takeIf { text.isNotEmpty() && context.canOpenUrl() }
            ) { url ->
                platformClickable(
                    onClick = { context.openUrl(url) },
                    onAlt2Click = { context.openUrl(url) }
                )
                .semantics {
                    if (linkContentDescription != null) {
                        contentDescription = linkContentDescription
                    }
                    role = Role.Button
                }
                .pointerHoverIcon(PointerIcon.Hand, true)
            }
    )
}
