package dev.toastbits.lifelog.extension.mediawatch.model.entity.event

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.string.StringId
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtensionStrings
import dev.toastbits.lifelog.extension.mediawatch.impl.mediaRangeToText
import dev.toastbits.lifelog.extension.mediawatch.impl.model.entity.event.MediaRangeValue
import dev.toastbits.lifelog.extension.mediawatch.model.reference.MediaReference
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType
import kotlin.time.Duration

data class MovieOrShowMediaConsumeEvent(
    override val extensionId: ExtensionId?,
    override var mediaReference: MediaReference,
    override var inlineComment: UserContent? = null,
    override var aboveComment: UserContent? = null,
    override var content: UserContent? = null,
    override var iteration: Int? = null,
    override var iterationsUnsure: Boolean = false,
    var watchedRange: WatchedRange? = null,
    var watchedRangeUnsure: Boolean = false
): MediaConsumeEvent {
    override val mediaEntityType: MediaEntityType = MediaEntityType.MOVIE_OR_SHOW

    override fun getIcon(): LogEvent.Icon = LogEvent.Icon.Movie

    override fun generateMediaRangeMetadata(
        strings: MediaWatchExtensionStrings,
        logStrings: LogFileConverterStrings
    ): String? {
        val range: WatchedRange = watchedRange ?: return null
        return buildString {
            if (watchedRangeUnsure) {
                append(strings.unsurePrefixes.first())
            }

            when (range) {
                is WatchedRange.Episodes -> {
                    append(strings.mediaRangeToText(mediaEntityType, range.startEpisode, range.endEpisode) ?: return null)
                }
                is WatchedRange.FirstEpisodes -> {
                    append(strings.mediaRangeFirstPrefixes.first())
                    append(strings.mediaRangeValueToText(range.episodes))
                    append(strings.movieOrShowEpisodeCountSuffixes.first())
                }
                is WatchedRange.LastEpisodes -> {
                    append(strings.movieOrShowLastEpisodesPrefixes.first())
                    append(strings.mediaRangeValueToText(range.episodes))
                    append(strings.movieOrShowEpisodeCountSuffixes.first())
                }

                is WatchedRange.Proportion -> append(strings.movieOrShowProportionWatchRangeToText(range))

                is WatchedRange.Times -> {
                    if (range.startTime != null && range.endTime != null) {
                        append(strings.mediaPreferredDurationFormat.format(range.startTime))
                        append(strings.mediaRangeSplitterDefaultMany)
                        append(strings.mediaPreferredDurationFormat.format(range.endTime))
                    }
                    else if (range.startTime != null) {
                        append(strings.mediaDurationRangeFromPrefixes.first())
                        append(strings.mediaPreferredDurationFormat.format(range.startTime))
                    }
                    else if (range.endTime != null) {
                        append(strings.mediaDurationRangeToPrefixes.first())
                        append(strings.mediaPreferredDurationFormat.format(range.endTime))
                    }
                }

                WatchedRange.Start -> strings.mediaRangeStart.first()
                WatchedRange.End -> strings.mediaRangeEnd.first()
            }
        }
    }

    override fun copy(
        content: UserContent?,
        inlineComment: UserContent?,
        aboveComment: UserContent?,
        properties: Map<StringId, LogEntity.Property<*, *>>?
    ): LogEvent =
        copy(
            mediaReference = mediaReference,
            inlineComment = inlineComment,
            aboveComment = aboveComment,
            content = content,
            iteration = iteration,
            iterationsUnsure = iterationsUnsure,
            watchedRange = watchedRange,
            watchedRangeUnsure = watchedRangeUnsure
        )

    sealed interface WatchedRange {
        data class Episodes(
            val startEpisode: MediaRangeValue?,
            val endEpisode: MediaRangeValue?
        ): WatchedRange

        data class FirstEpisodes(val episodes: MediaRangeValue): WatchedRange
        data class LastEpisodes(val episodes: MediaRangeValue): WatchedRange

        data class Times(val startTime: Duration?, val endTime: Duration?): WatchedRange

        enum class Proportion: WatchedRange {
            FIRST_HALF,
            SECOND_HALF
        }

        data object Start: WatchedRange
        data object End: WatchedRange
    }
}
