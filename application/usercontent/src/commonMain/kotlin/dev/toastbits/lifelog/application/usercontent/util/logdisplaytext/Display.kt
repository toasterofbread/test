package dev.toastbits.lifelog.application.usercontent.util.logdisplaytext

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.toastbits.lifelog.application.usercontent.UserContentDisplay
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText

@Composable
fun LogDisplayText.Display(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current
) {
    when (this) {
        is LogDisplayText.OfString -> Text(string, modifier, style = textStyle)
        is LogDisplayText.OfUserContent -> UserContentDisplay(userContent, modifier, textStyle = textStyle)
    }
}
