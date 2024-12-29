package dev.toastbits.lifelog.application.logview.component.timeline.item

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.toastbits.lifelog.application.settings.data.compositionlocal.LocalSettings
import dev.toastbits.lifelog.application.settings.domain.appsettings.AppSettings
import dev.toastbits.lifelog.application.settings.domain.model.DisplayDateFormat
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate

data class DateTimelineItem(
    val date: LogDate,
    val index: Int
): TimelineItem {
    @Composable
    override fun MainContent(modifier: Modifier, onIsSingleLineChanged: ((Boolean) -> Unit)?) {
        val settings: AppSettings = LocalSettings.current
        val dateFormat: DisplayDateFormat by settings.Display.DATE_FORMAT.observe()

        val dateString: String =
            remember(date.date, dateFormat) {
                dateFormat.getLocalDateFormat().format(date.date)
            }

        Text(
            dateString,
            modifier,
            onTextLayout =
                if (onIsSingleLineChanged == null) null
                else {{
                    onIsSingleLineChanged(it.lineCount <= 1)
                }}
        )
    }

    @Composable
    override fun MetadataItems(itemModifier: Modifier) {}

    @Composable
    override fun IconContent(modifier: Modifier) {
        HorizontalDivider(modifier)
    }

    override val hasWideIcon: Boolean = true
}
