package dev.toastbits.lifelog.extension.mediawatch.impl.model.entity.event

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtensionStrings
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.GameMediaConsumeEvent

internal fun applyGameEventMetadata(
    text: String,
    event: GameMediaConsumeEvent,
    strings: MediaWatchExtensionStrings,
    logStrings: LogFileConverterStrings,
    onAlert: (LogParseAlert) -> Unit
): GameMediaConsumeEvent {
    var currentEvent: GameMediaConsumeEvent = event
    val parts: List<String> = text.split(',')

    for (part in parts) {
        if (currentEvent.playedRange == null) {
            val newEvent: GameMediaConsumeEvent? =
                applyEventPlayedRangeString(text, currentEvent, strings, logStrings, onAlert)

            if (newEvent != null) {
                currentEvent = newEvent
                continue
            }
        }

        TODO("$part | $text")
    }

    return currentEvent
}

private fun applyEventPlayedRangeString(
    text: String,
    event: GameMediaConsumeEvent,
    strings: MediaWatchExtensionStrings,
    logStrings: LogFileConverterStrings,
    onAlert: (LogParseAlert) -> Unit
): GameMediaConsumeEvent? {
    val range: GameMediaConsumeEvent.PlayedRange? = strings.parseLowercaseGamePlayedRange(text, logStrings)
    if (range != null) {
        return event.copy(
            playedRange = range
        )
    }

    return null
}
