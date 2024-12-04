package dev.toastbits.lifelog.application.logview.data.ui.component.event

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
import dev.toastbits.lifelog.application.core.model.LogEventData
import dev.toastbits.lifelog.application.core.model.formatDate
import dev.toastbits.lifelog.application.core.ui.util.rememberLocalisedString
import dev.toastbits.lifelog.application.usercontent.util.logdisplaytext.Display
import dev.toastbits.lifelog.application.usercontent.util.logdisplaytext.rememberLocalisedDisplayTextNullable
import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import lifelog.application.logview.data.generated.resources.Res
import lifelog.application.logview.data.generated.resources.`event_screen_metadata_heading_$verb_on_$date`
import org.jetbrains.compose.resources.stringResource

@Composable
fun LogEventMetadata(
    event: LogEventData,
    modifier: Modifier = Modifier
) {
    val extension: SpecificationExtension? =
        event.extensionId?.let {
            event.configuration.extensionRegistry.findRegisteredExtension(it)
        }

    SelectionContainer(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val typeVerb: String by event.typeVerb.rememberLocalisedString(extension)
            val dateString: String = remember(event) { event.formatDate() }
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
