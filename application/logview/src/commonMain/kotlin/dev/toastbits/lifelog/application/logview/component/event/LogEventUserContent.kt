package dev.toastbits.lifelog.application.logview.component.event

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.utils.composable.SubtleLoadingIndicator
import dev.toastbits.composekit.components.utils.composable.crossfade.NullCrossfade
import dev.toastbits.lifelog.application.logview.model.LogEventViewScreenState
import dev.toastbits.lifelog.application.logview.model.rememberLoadedOrNull
import dev.toastbits.lifelog.application.usercontent.UserContentDisplay

@Composable
internal fun LogEventUserContent(
    state: LogEventViewScreenState,
    modifier: Modifier = Modifier,
    updateState: (LogEventViewScreenState.Loaded) -> Unit
) {
    val loadedState: LogEventViewScreenState.Loaded? by state.rememberLoadedOrNull()

    NullCrossfade(
        loadedState,
        modifier.fillMaxWidth()
    ) { loaded ->
        if (loaded == null) {
            SubtleLoadingIndicator(Modifier.padding(top = 50.dp))
        }
        else {
            when (loaded) {
                is LogEventViewScreenState.Loaded.Edit -> {
                    val textFieldState: TextFieldState = remember { TextFieldState(loaded.content) }

                    BasicTextField(
                        textFieldState,
                        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                        cursorBrush = SolidColor(LocalContentColor.current)
                    )

                    LaunchedEffect(textFieldState.text) {
                        updateState(loaded.copy(textFieldState.text.toString()))
                    }
                }
                is LogEventViewScreenState.Loaded.Preview -> UserContentDisplay(loaded.content)
            }
        }
    }
}
