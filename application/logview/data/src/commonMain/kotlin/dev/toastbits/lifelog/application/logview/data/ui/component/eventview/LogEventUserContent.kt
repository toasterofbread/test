package dev.toastbits.lifelog.application.logview.data.ui.component.eventview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier
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
                is LogEventViewScreenState.Editing ->
                    BasicTextField(
                        currentState.textFieldState,
                        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                        cursorBrush = SolidColor(LocalContentColor.current)
                    )
                is LogEventViewScreenState.Previewing ->
                    UserContentDisplay(
                        currentState.content
                    )
            }
        }
    }
}
