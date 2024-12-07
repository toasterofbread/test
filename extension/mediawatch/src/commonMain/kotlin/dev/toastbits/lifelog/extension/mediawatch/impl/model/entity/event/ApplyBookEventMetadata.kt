package dev.toastbits.lifelog.extension.mediawatch.impl.model.entity.event

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtensionStrings
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.BookMediaConsumeEvent

internal fun applyBookEventMetadata(
    text: String,
    event: BookMediaConsumeEvent,
    strings: MediaWatchExtensionStrings,
    logStrings: LogFileConverterStrings,
    onAlert: (LogParseAlert) -> Unit
): BookMediaConsumeEvent {
    var currentEvent: BookMediaConsumeEvent = event
    val parts: List<String> = text.split(',')

    for (part in parts) {
        val newEvent: BookMediaConsumeEvent? =
            applyEventReadRangeString(text, currentEvent, strings, logStrings, onAlert)

        if (newEvent != null) {
            currentEvent = newEvent
            continue
        }

        TODO("$part | $text")
    }

    return currentEvent
}

private fun applyEventReadRangeString(
    text: String,
    event: BookMediaConsumeEvent,
    strings: MediaWatchExtensionStrings,
    logStrings: LogFileConverterStrings,
    onAlert: (LogParseAlert) -> Unit
): BookMediaConsumeEvent? {
    val range: BookMediaConsumeEvent.ReadRange? = strings.parseLowercaseBookReadRange(text, logStrings, onAlert)
    if (range != null) {
        return event.copy(
            readRange = range
        )
    }

    return null
}
