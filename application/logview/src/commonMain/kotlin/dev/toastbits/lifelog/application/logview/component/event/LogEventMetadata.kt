package dev.toastbits.lifelog.application.logview.component.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import dev.toastbits.lifelog.application.core.ui.util.rememberLocalisedString
import dev.toastbits.lifelog.application.usercontent.util.logdisplaytext.Display
import dev.toastbits.lifelog.application.usercontent.util.logdisplaytext.rememberLocalisedDisplayTextNullable
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.application.logview.generated.resources.Res
import dev.toastbits.lifelog.application.logview.generated.resources.`event_screen_metadata_heading_$verb_on_$date`
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T: LogEvent> LogEventMetadata(
    event: T,
    date: LogDate,
    configuration: LogDatabaseConfiguration,
    modifier: Modifier = Modifier
) {
    SelectionContainer(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val typeVerb: String by event.typeVerb.rememberLocalisedString(configuration)
            val dateString: String = remember(event) {
                configuration.strings.preferredDateFormat.format(date.date)
            }

            Text(
                stringResource(Res.string.`event_screen_metadata_heading_$verb_on_$date`)
                    .replace("\$verb", typeVerb)
                    .replace("\$date", dateString),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.alpha(0.7f)
            )

            val eventTitle: LogDisplayText? by event::getTitle.rememberLocalisedDisplayTextNullable()
            eventTitle?.Display(textStyle = MaterialTheme.typography.headlineLarge)
        }
    }
}
