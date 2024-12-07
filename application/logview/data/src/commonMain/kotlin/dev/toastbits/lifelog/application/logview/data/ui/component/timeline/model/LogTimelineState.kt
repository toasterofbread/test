package dev.toastbits.lifelog.application.logview.data.ui.component.timeline.model

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class LogTimelineState(
    firstVisibleItemIndex: Int = 0,
    from: LogTimelineState? = null
) {
    val columnState: LazyListState =
        LazyListState(
            from?.columnState?.firstVisibleItemIndex ?: firstVisibleItemIndex,
            from?.columnState?.firstVisibleItemScrollOffset ?: 0
        )
    var waveOffset: Float by mutableFloatStateOf(from?.waveOffset ?: 0f)
    var filterText: String? by mutableStateOf(from?.filterText ?: "やがて")
}
