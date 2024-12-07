package dev.toastbits.lifelog.extension.mediawatch.model.entity.event

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtension
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtensionStrings
import dev.toastbits.lifelog.extension.mediawatch.model.reference.MediaReference
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType
import kotlinx.datetime.LocalDate

data class GameMediaConsumeEvent(
    override var mediaReference: MediaReference,
    override var inlineComment: UserContent? = null,
    override var aboveComment: UserContent? = null,
    override var content: UserContent? = null,
    override var iteration: Int? = null,
    override var iterationsUnsure: Boolean = false,
    var playedRange: PlayedRange? = null
): MediaConsumeEvent {
    override val extensionId: ExtensionId = MediaWatchExtension.ID
    override val mediaEntityType: MediaEntityType = MediaEntityType.GAME

    override fun getIcon(): LogEvent.Icon = LogEvent.Icon.Gamepad

    override fun generateMediaRangeMetadata(
        strings: MediaWatchExtensionStrings,
        logStrings: LogFileConverterStrings
    ): String? = buildString {
        val range: PlayedRange = playedRange ?: return null

        when (range) {
            PlayedRange.Start -> append(strings.mediaRangeStart.first())
            PlayedRange.End -> append(strings.mediaRangeEnd.first())
            is PlayedRange.Days -> {
                if (range.startDay != null && range.endDay != null) {
                    append(logStrings.preferredDateFormat.format(range.startDay))
                    if (range.unsure) {
                        append(strings.unsurePrefixes.first())
                    }
                    append(strings.mediaRangeSplitterDefaultMany)
                    append(logStrings.preferredDateFormat.format(range.endDay))
                }
                else if (range.startDay != null) {
                    append(strings.mediaDurationRangeFromPrefixes.first())
                    if (range.unsure) {
                        append(strings.unsurePrefixes.first())
                    }
                    append(logStrings.preferredDateFormat.format(range.startDay))
                }
                else if (range.endDay != null) {
                    append(strings.mediaDurationRangeToPrefixes.first())
                    if (range.unsure) {
                        append(strings.unsurePrefixes.first())
                    }
                    append(logStrings.preferredDateFormat.format(range.endDay))
                }
                else {
                    return null
                }
            }
            is PlayedRange.Duration -> {
                if (range.unsure) {
                    append(strings.unsurePrefixes.first())
                }
                append(strings.mediaPreferredDurationFormat.format(range.duration))
            }
        }
    }

    sealed interface PlayedRange {
        val unsure: Boolean

        data class Duration(val duration: kotlin.time.Duration, override val unsure: Boolean = false): PlayedRange
        data class Days(val startDay: LocalDate?, val endDay: LocalDate?, override val unsure: Boolean = false): PlayedRange

        data object Start: PlayedRange {
            override val unsure: Boolean = false
        }
        data object End: PlayedRange {
            override val unsure: Boolean = false
        }
    }

    override fun copy(inlineComment: UserContent?, aboveComment: UserContent?): MediaConsumeEvent =
        copy(
            mediaReference = mediaReference,
            inlineComment = inlineComment,
            aboveComment = aboveComment,
            content = content,
            iteration = iteration,
            iterationsUnsure = iterationsUnsure,
            playedRange = playedRange
        )

    override fun copy(content: UserContent?): MediaConsumeEvent =
        copy(
            mediaReference = mediaReference,
            inlineComment = inlineComment,
            aboveComment = aboveComment,
            content = content,
            iteration = iteration,
            iterationsUnsure = iterationsUnsure,
            playedRange = playedRange
        )

    override fun copy(
        mediaReference: MediaReference,
        iteration: Int?,
        iterationsUnsure: Boolean
    ): MediaConsumeEvent =
        copy(
            mediaReference = mediaReference,
            inlineComment = inlineComment,
            aboveComment = aboveComment,
            content = content,
            iteration = iteration,
            iterationsUnsure = iterationsUnsure,
            playedRange = playedRange
        )
}
