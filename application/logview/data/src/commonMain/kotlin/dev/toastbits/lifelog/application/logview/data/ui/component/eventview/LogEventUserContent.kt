package dev.toastbits.lifelog.application.logview.data.ui.component.eventview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.utils.composable.SubtleLoadingIndicator
import dev.toastbits.composekit.util.composable.AlignableCrossfade
import dev.toastbits.lifelog.application.usercontent.UserContentDisplay

@Composable
internal fun LogEventUserContent(
    state: LogEventViewScreenState,
    loadingNextStateType: LogEventViewScreenState.Type?,
    modifier: Modifier = Modifier,
    updateState: (LogEventViewScreenState) -> Unit
) {
    AlignableCrossfade(
        loadingNextStateType,
        modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) { loadingType ->
        if (loadingType != null) {
            SubtleLoadingIndicator(Modifier.padding(top = 50.dp))
        }
        else {
            when (val currentState: LogEventViewScreenState = state) {
                is LogEventViewScreenState.Editing -> {
                    val textFieldState: TextFieldState = remember { TextFieldState(currentState.content) }

                    BasicTextField(
                        textFieldState,
                        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                        cursorBrush = SolidColor(LocalContentColor.current)
                    )

                    LaunchedEffect(textFieldState.text) {
                        updateState(currentState.copy(textFieldState.text.toString()))
                    }
                }
                is LogEventViewScreenState.Previewing ->
                    UserContentDisplay(
                        currentState.content
                    )
            }
        }
    }
}
