package dev.toastbits.lifelog.extension.mediawatch

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.extension.mediawatch.impl.model.MediaDurationFormat
import dev.toastbits.lifelog.extension.mediawatch.impl.model.entity.event.MediaRangeValue
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.BookMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.GameMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.MovieOrShowMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType

interface MediaWatchExtensionStrings {
    val mediaReferenceTypeId: String
    val mediaPreferredDurationFormat: MediaDurationFormat
    val mediaDurationRangeFromPrefixes: List<String>
    val mediaDurationRangeToPrefixes: List<String>
    val mediaDurationFormats: List<MediaDurationFormat>
    val mediaRangeStart: List<String>
    val mediaRangeEnd: List<String>

    val mediaRangeSplitters: List<String>
    val mediaRangeSplitterDefaultTwo: String
    val mediaRangeSplitterDefaultMany: String

    val mediaRangeFirstPrefixes: List<String>
    val unsurePrefixes: List<String>

    val movieOrShowEpisodeRangePrefixes: List<String>
    val movieOrShowLastEpisodesPrefixes: List<String>
    val movieOrShowEpisodeCountSuffixes: List<String>

    val bookReadVolumePrefixes: List<String>
    val bookReadChapterPrefixes: List<String>
    val bookReadPagePrefixes: List<String>

    fun getMediaEntityTypeIterationSuffixes(mediaEntityType: MediaEntityType): List<String>
    fun getMediaEntityTypeConsumeEventPrefixes(mediaEntityType: MediaEntityType): List<String>

    fun parseLowercaseMovieOrShowWatchedRange(text: String, strings: LogFileConverterStrings): MovieOrShowMediaConsumeEvent.WatchedRange?
    fun parseLowercaseBookReadRange(text: String, strings: LogFileConverterStrings, onAlert: (LogParseAlert) -> Unit): BookMediaConsumeEvent.ReadRange?
    fun parseLowercaseGamePlayedRange(text: String, strings: LogFileConverterStrings): GameMediaConsumeEvent.PlayedRange?

    fun movieOrShowProportionWatchRangeToText(range: MovieOrShowMediaConsumeEvent.WatchedRange.Proportion): String

    fun mediaRangeValueToText(rangeValue: MediaRangeValue): String
}
