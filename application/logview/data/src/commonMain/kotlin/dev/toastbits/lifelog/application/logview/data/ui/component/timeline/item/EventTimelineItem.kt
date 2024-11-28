package dev.toastbits.lifelog.application.logview.data.ui.component.timeline.item

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.composekit.util.rememberAsLocalisedValue
import dev.toastbits.composekit.util.thenIf
import dev.toastbits.lifelog.application.usercontent.UserContentDisplay
import dev.toastbits.lifelog.application.logview.data.ui.screen.LogEventReference
import dev.toastbits.lifelog.application.logview.data.ui.screen.get
import dev.toastbits.lifelog.application.logview.data.ui.toImageVector
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.event.LogCommentEvent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent

@Composable
fun (suspend (String) -> LogDisplayText).rememberAsLogDisplayText(getDefault: () -> String = { "" }): State<LogDisplayText> =
    this.rememberAsLocalisedValue { LogDisplayText.OfString(getDefault()) }

@Composable
fun LogDisplayText.Display(modifier: Modifier = Modifier) {
    when (this) {
        is LogDisplayText.OfString -> Text(string, modifier)
        is LogDisplayText.OfUserContent -> UserContentDisplay(userContent, modifier)
    }
}

data class EventTimelineItem(
    val eventReference: LogEventReference,
    val logDatabase: LogDatabase
): TimelineItem {
    val event: LogEvent
        get() = logDatabase[eventReference]

    @Composable
    override fun MainContent(modifier: Modifier) {
        val title: LogDisplayText by event::getTitle.rememberAsLogDisplayText()
        title.Display(modifier)
    }

    @Composable
    override fun MetadataItems(itemModifier: Modifier) {
        if (event is LogCommentEvent) {
            return
        }

        Text("What", itemModifier)
        Text("The", itemModifier)
        Text("Metadata", itemModifier)
        Text("Item", itemModifier)
    }

    @Composable
    override fun IconContent(modifier: Modifier) {
        val theme: ThemeValues = LocalComposeKitTheme.current
        val shape: Shape = CircleShape

        Box(
            modifier
                .background(theme.background, shape)
                .thenIf(event !is LogCommentEvent) {
                    border(2.dp, theme.accent, shape)
                    .clip(shape)
                }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (event !is LogCommentEvent) {
                Icon(event.getIcon().toImageVector(), null, tint = theme.onBackground)
            }
            else {
                Canvas(
                    Modifier
                        .padding(5.dp)
                        .size(20.dp)
                ) {
                    translate {
                        rotate(-65f) {
                            drawLine(
                                theme.accent,
                                Offset(0f, size.height / 2f),
                                Offset(size.width, size.height / 2f),
                                cap = StrokeCap.Round,
                                strokeWidth = 2.dp.toPx()
                            )
                            translate(top = 5.dp.toPx()) {
                                drawLine(
                                    theme.accent,
                                    Offset(0f, size.height / 2f),
                                    Offset(size.width, size.height / 2f),
                                    cap = StrokeCap.Round,
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
