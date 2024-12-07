package dev.toastbits.lifelog.extension.mediawatch.impl.model.entity.event

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtensionStrings
import dev.toastbits.lifelog.extension.mediawatch.alert.MediaWatchLogParseAlert
import dev.toastbits.lifelog.extension.mediawatch.impl.model.parseOrNull
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.MovieOrShowMediaConsumeEvent
import kotlin.time.Duration

internal fun applyMovieOrShowEventMetadata(
    text: String,
    event: MovieOrShowMediaConsumeEvent,
    strings: MediaWatchExtensionStrings,
    logStrings: LogFileConverterStrings,
    onAlert: (LogParseAlert) -> Unit
): MovieOrShowMediaConsumeEvent {
    var currentEvent: MovieOrShowMediaConsumeEvent = event
    val parts: List<String> = text.split(',')

    for (part in parts) {
        if (currentEvent.watchedRange == null) {
            val newEvent: MovieOrShowMediaConsumeEvent? =
                applyEventWatchedRangeString(text, currentEvent, strings, logStrings, onAlert)

            if (newEvent != null) {
                currentEvent = newEvent
                continue
            }
        }

        TODO("$part | $text")
    }

    return currentEvent
}

private fun applyEventWatchedRangeString(
    text: String,
    event: MovieOrShowMediaConsumeEvent,
    strings: MediaWatchExtensionStrings,
    logStrings: LogFileConverterStrings,
    onAlert: (LogParseAlert) -> Unit
): MovieOrShowMediaConsumeEvent? {
    var currentEvent: MovieOrShowMediaConsumeEvent = event

    var text: String = text
    for (prefix in strings.unsurePrefixes) {
        if (text.startsWith(prefix)) {
            currentEvent = currentEvent.copy(watchedRangeUnsure = true)
            text = text.drop(prefix.length).trimStart()
        }
    }

    val range: MovieOrShowMediaConsumeEvent.WatchedRange? = strings.parseLowercaseMovieOrShowWatchedRange(text, logStrings)
    if (range != null) {
        return currentEvent.copy(watchedRange = range)
    }

    for (prefix in strings.movieOrShowEpisodeRangePrefixes) {
        if (text.startsWith(prefix)) {
            val episodeRangeText: String = text.drop(prefix.length).trimStart()

            val (lhs: MediaRangeValue?, rhs: MediaRangeValue?) = parseMediaRangeString(episodeRangeText, strings, onAlert)
            return currentEvent.copy(
                watchedRange = MovieOrShowMediaConsumeEvent.WatchedRange.Episodes(lhs, rhs)
            )
        }
    }

    for (prefix in strings.mediaRangeFirstPrefixes) {
        if (!text.startsWith(prefix)) {
            continue
        }

        val text: String = text.drop(prefix.length).trimStart()

        for (episodePrefix in strings.movieOrShowEpisodeRangePrefixes) {
            if (!text.startsWith(episodePrefix)) {
                continue
            }

            val episodeRangeText: String = text.drop(episodePrefix.length).trimStart()
            val rangeValue: MediaRangeValue? = MediaRangeValue.fromString(episodeRangeText)
            if (rangeValue != null) {
                currentEvent = currentEvent.copy(watchedRange = MovieOrShowMediaConsumeEvent.WatchedRange.Episodes(startEpisode = null, endEpisode = rangeValue))
            }

            return currentEvent
        }

        return applyEventFirstDurationRangeString(text, currentEvent, strings, onAlert)
    }

    for (prefix in strings.mediaDurationRangeFromPrefixes) {
        if (text.startsWith(prefix)) {
            val durationRangeText: String = text.drop(prefix.length).trimStart()
            return applyEventFromDurationRangeString(durationRangeText, currentEvent, strings, onAlert)
        }
    }

    return null
}

private fun applyEventFirstDurationRangeString(
    text: String,
    event: MovieOrShowMediaConsumeEvent,
    strings: MediaWatchExtensionStrings,
    onAlert: (LogParseAlert) -> Unit
): MovieOrShowMediaConsumeEvent {
    val duration: Duration? = strings.mediaDurationFormats.firstNotNullOfOrNull { it.parseOrNull(text) }
    if (duration == null) {
        onAlert(MediaWatchLogParseAlert.UnknownDurationFormat(text))
        return event
    }

    return event.copy(
        watchedRange =
        MovieOrShowMediaConsumeEvent.WatchedRange.Times(
            startTime = null,
            endTime = duration
        )
    )
}

private fun applyEventFromDurationRangeString(
    text: String,
    event: MovieOrShowMediaConsumeEvent,
    strings: MediaWatchExtensionStrings,
    onAlert: (LogParseAlert) -> Unit
): MovieOrShowMediaConsumeEvent {
    val duration: Duration? = strings.mediaDurationFormats.firstNotNullOfOrNull { it.parseOrNull(text) }
    if (duration == null) {
        onAlert(MediaWatchLogParseAlert.UnknownDurationFormat(text))
        return event
    }

    return event.copy(
        watchedRange =
            MovieOrShowMediaConsumeEvent.WatchedRange.Times(
                startTime = duration,
                endTime = null
            )
    )
}

