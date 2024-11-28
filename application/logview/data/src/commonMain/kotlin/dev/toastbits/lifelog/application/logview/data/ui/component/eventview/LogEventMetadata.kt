package dev.toastbits.lifelog.application.logview.data.ui.component.eventview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.toastbits.composekit.util.getValue
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.item.Display
import dev.toastbits.lifelog.application.logview.data.ui.component.timeline.item.rememberAsLogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent

@Composable
fun LogEventMetadata(
    event: LogEvent,
    modifier: Modifier = Modifier
) {
    SelectionContainer(modifier) {
        Column {
            val title: LogDisplayText? by event::getTitle.rememberAsLogDisplayText()
            title?.Display(textStyle = MaterialTheme.typography.headlineLarge)

            // TODO
        }
    }
}
