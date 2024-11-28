package dev.toastbits.lifelog.application.logview.data.ui.component.timeline

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.model.LogTimelineState

@Composable
internal fun LogTimelineSearchField(
    timelineState: LogTimelineState,
    focusImmediately: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester: FocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (focusImmediately) {
            focusRequester.requestFocus()
        }
    }

    TextField(
        timelineState.filterText.orEmpty(),
        onValueChange = {
            timelineState.filterText = it
        },
        modifier = modifier.focusRequester(focusRequester),
        trailingIcon = {
            Row {
                IconButton({ timelineState.filterText = null }) {
                    Icon(Icons.Default.Close, null) // TODO
                }
                IconButton(onClose) {
                    Icon(Icons.Default.KeyboardArrowDown, null) // TODO
                }
            }
        }
    )
}
