package dev.toastbits.lifelog.application.usercontent.util.logdisplaytext

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.toastbits.lifelog.application.usercontent.UserContentDisplay
import dev.toastbits.lifelog.application.usercontent.UserContentDisplayResult
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LogDisplayText.Display(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    onIsSingleLineChanged: ((Boolean) -> Unit)? = null
) {
    when (this) {
        is LogDisplayText.OfString ->
            Text(
                string,
                modifier,
                style = textStyle,
                onTextLayout =
                    if (onIsSingleLineChanged != null) {{
                        onIsSingleLineChanged(it.lineCount <= 1)
                    }}
                    else null
            )
        is LogDisplayText.OfUserContent -> {
            UserContentDisplay(
                userContent,
                modifier,
                textStyle = textStyle,
                onDisplayResult =
                    if (onIsSingleLineChanged == null) null
                    else {{ displayResult ->
                        onIsSingleLineChanged(displayResult.singleLine)
                    }}
            )
        }
    }
}
