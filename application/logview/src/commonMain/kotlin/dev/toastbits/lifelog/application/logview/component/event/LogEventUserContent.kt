package dev.toastbits.lifelog.application.logview.component.event

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.utils.composable.SubtleLoadingIndicator
import dev.toastbits.composekit.components.utils.composable.crossfade.NullCrossfade
import dev.toastbits.lifelog.application.logview.model.LogEventViewContentState
import dev.toastbits.lifelog.application.usercontent.UserContentDisplay

@Composable
internal fun LogEventUserContent(
    state: LogEventViewContentState?,
    modifier: Modifier = Modifier,
    key1: Any? = Unit,
    updateState: (LogEventViewContentState) -> Unit
) {
    NullCrossfade(
        state,
        modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        key1 = key1
    ) { currentState ->
        when (currentState) {
            null ->
                SubtleLoadingIndicator(Modifier.padding(top = 50.dp))

            is LogEventViewContentState.Edit -> {
                val textFieldState: TextFieldState = remember { TextFieldState(currentState.content) }

                BasicTextField(
                    textFieldState,
                    Modifier.fillMaxSize(),
                    textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                    cursorBrush = SolidColor(LocalContentColor.current)
                )

                LaunchedEffect(textFieldState.text) {
                    updateState(currentState.copy(textFieldState.text.toString()))
                }
            }

            is LogEventViewContentState.Preview ->
                UserContentDisplay(currentState.content, Modifier.fillMaxSize())
        }
    }
}
