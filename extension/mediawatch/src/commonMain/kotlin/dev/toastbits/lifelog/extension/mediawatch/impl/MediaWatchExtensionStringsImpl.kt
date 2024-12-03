package dev.toastbits.lifelog.extension.mediawatch.impl

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.converter.parseOrNull
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtensionStrings
import dev.toastbits.lifelog.extension.mediawatch.alert.MediaWatchLogParseAlert
import dev.toastbits.lifelog.extension.mediawatch.impl.model.MediaDurationFormat
import dev.toastbits.lifelog.extension.mediawatch.impl.model.customMediaDurationFormat
import dev.toastbits.lifelog.extension.mediawatch.impl.model.durationFormatOf
import dev.toastbits.lifelog.extension.mediawatch.impl.model.entity.event.MediaRangeValue
import dev.toastbits.lifelog.extension.mediawatch.impl.model.entity.event.mediaRangeValue
import dev.toastbits.lifelog.extension.mediawatch.impl.model.parseOrNull
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.BookMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.GameMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.MovieOrShowMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.format.optional
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class MediaWatchExtensionStringsImpl(
    override val extensionId: ExtensionId = "mediawatch",
    override val mediaReferenceTypeId: ExtensionId = "media",

    override val unsurePrefixes: List<String> = listOf("about ", "at least "),

    override val mediaRangeSplitters: List<String> = listOf("~", "-", "〰", "〜", "&", "and"),
    override val mediaRangeSplitterDefaultTwo: String = " and ",
    override val mediaRangeSplitterDefaultMany: String = "~",
    override val mediaRangeStart: List<String> = listOf("start"),
    override val mediaRangeEnd: List<String> = listOf("end"),

    override val mediaRangeFirstPrefixes: List<String> = listOf("first ", "up to "),
    override val mediaPreferredDurationFormat: MediaDurationFormat = durationFormatOf(LocalTime.Formats.ISO),
    override val mediaDurationRangeFromPrefixes: List<String> = listOf("from ", "starting ", "starting at "),
    override val mediaDurationRangeToPrefixes: List<String> = listOf("up to ", "to "),
    override val mediaDurationFormats: List<MediaDurationFormat> = listOf(
        durationFormatOf(LocalTime.Formats.ISO),
        durationFormatOf {
            hour(Padding.NONE)
            char('h')
            optional {
                char(' ')
            }
            minute(Padding.NONE)
            char('m')
        },
        durationFormatOf {
            hour(Padding.NONE)
            char(':')
            minute()
            char(':')
            second()
        },
        customMediaDurationFormat(
            format = { duration ->
                "${duration.inWholeMinutes} minutes"
            },
            parse = { input ->
                check(input.lowercase().endsWith(" minutes"))
                input.dropLast(8).trimEnd().toInt().minutes
            }
        )
    ),

    override val movieOrShowEpisodeRangePrefixes: List<String> = listOf("ep ", "eps ", "episode ", "episodes "),
    override val movieOrShowLastEpisodesPrefixes: List<String> = listOf("last "),
    override val movieOrShowEpisodeCountSuffixes: List<String> = listOf(" episodes"),

    override val bookReadVolumePrefixes: List<String> = listOf("book ", "books ", "volume ", "volumes "),
    override val bookReadChapterPrefixes: List<String> = listOf("chapter ", "chapters"),
    override val bookReadPagePrefixes: List<String> = listOf("page ", "pages ", "pg ", "pgs ")
): MediaWatchExtensionStrings {
    override fun getMediaEntityTypeIterationSuffixes(mediaEntityType: MediaEntityType): List<String> =
        when (mediaEntityType) {
            MediaEntityType.MOVIE_OR_SHOW -> listOf(" watch")
            MediaEntityType.BOOK -> listOf(" read")
            MediaEntityType.GAME -> listOf(" play", " attempt", " run")
            MediaEntityType.SONG -> listOf(" listen")
        }

    override fun getMediaEntityTypeConsumeEventPrefixes(mediaEntityType: MediaEntityType): List<String> =
        when (mediaEntityType) {
            MediaEntityType.MOVIE_OR_SHOW -> listOf("Watched ")
            MediaEntityType.BOOK -> listOf("Read ")
            MediaEntityType.GAME -> listOf("Played ")
            MediaEntityType.SONG -> listOf("Listened to ")
        }

    override fun parseLowercaseMovieOrShowWatchedRange(text: String, strings: LogFileConverterStrings): MovieOrShowMediaConsumeEvent.WatchedRange? {
        when (text) {
            "start" -> return MovieOrShowMediaConsumeEvent.WatchedRange.Start
            "end",
            "完" -> return MovieOrShowMediaConsumeEvent.WatchedRange.End
            "first half",
            "half" -> return MovieOrShowMediaConsumeEvent.WatchedRange.Proportion.FIRST_HALF
            "second half" -> return MovieOrShowMediaConsumeEvent.WatchedRange.Proportion.SECOND_HALF
            "first episode" -> return MovieOrShowMediaConsumeEvent.WatchedRange.Episodes(startEpisode = 1U.mediaRangeValue, endEpisode = 1U.mediaRangeValue)
        }

        var text: String =
            movieOrShowEpisodeCountSuffixes.firstNotNullOfOrNull { prefix ->
                if (text.endsWith(prefix)) {
                    return@firstNotNullOfOrNull text.dropLast(prefix.length).trimEnd()
                }
                return@firstNotNullOfOrNull null
            } ?: return null

        var first: Boolean = true

        if (text.startsWith("first ")) {
            text = text.drop(6).trimStart()
        }
        else {
            for (prefix in movieOrShowLastEpisodesPrefixes) {
                if (text.startsWith(prefix)) {
                    text = text.drop(prefix.length).trimStart()
                    first = false
                }
            }
        }

        val singleValue: MediaRangeValue? = MediaRangeValue.fromString(text)
        if (singleValue != null) {
            if (first) {
                return MovieOrShowMediaConsumeEvent.WatchedRange.FirstEpisodes(singleValue)
            }
            else {
                return MovieOrShowMediaConsumeEvent.WatchedRange.LastEpisodes(singleValue)
            }
        }

        return null
    }

    override fun parseLowercaseBookReadRange(text: String, strings: LogFileConverterStrings, onAlert: (LogParseAlert) -> Unit): BookMediaConsumeEvent.ReadRange? {
        if (mediaRangeStart.contains(text)) {
            return BookMediaConsumeEvent.ReadRange(start = BookMediaConsumeEvent.ReadPoint.Start, end = null, unsure = false)
        }
        if (mediaRangeEnd.contains(text)) {
            return BookMediaConsumeEvent.ReadRange(start = null, end = BookMediaConsumeEvent.ReadPoint.End, unsure = false)
        }

        var upTo: Boolean = false
        var unsure: Boolean = false
        var from: Boolean = false
        var text: String = text

        for (prefix in mediaDurationRangeFromPrefixes) {
            if (text.startsWith(prefix)) {
                from = true
                text = text.drop(prefix.length).trimStart()
            }
        }

        for (prefix in unsurePrefixes) {
            if (text.startsWith(prefix)) {
                unsure = true
                text = text.drop(prefix.length).trimStart()
            }
        }

        for (prefix in mediaRangeFirstPrefixes) {
            if (text.startsWith(prefix)) {
                upTo = true
                text = text.drop(prefix.length).trimStart()
            }
        }

        val (splitter: String?, splitterIndex: Int?) =
            mediaRangeSplitters.firstNotNullOfOrNull { splitter ->
                val index: Int = text.indexOf(splitter)
                if (index == -1) {
                    return@firstNotNullOfOrNull null
                }
                return@firstNotNullOfOrNull splitter to index
            } ?: (null to null)

        val lhs: String
        val rhs: String?

        if (splitter == null) {
            lhs = text
            rhs = null
        }
        else {
            lhs = text.substring(0, splitterIndex!!)
            rhs = text.substring(splitterIndex + splitter.length)
        }

        val start: BookMediaConsumeEvent.ReadPoint? = parseBookReadPointString(lhs, null, onAlert)
        val end: BookMediaConsumeEvent.ReadPoint? = rhs?.let { parseBookReadPointString(it, start, onAlert) }

        if (upTo && end == null) {
            return BookMediaConsumeEvent.ReadRange(start = null, end = start, unsure = unsure)
        }
        else if (from && end == null) {
            return BookMediaConsumeEvent.ReadRange(start = start, end = null, unsure = unsure)
        }

        return BookMediaConsumeEvent.ReadRange(start = start, end = end, unsure = unsure)
    }

    private fun parseBookReadPointString(
        _text: String,
        previousPoint: BookMediaConsumeEvent.ReadPoint?,
        onAlert: (LogParseAlert) -> Unit
    ): BookMediaConsumeEvent.ReadPoint? {
        if (mediaRangeStart.contains(_text)) {
            return BookMediaConsumeEvent.ReadPoint.Start
        }
        if (mediaRangeEnd.contains(_text)) {
            return BookMediaConsumeEvent.ReadPoint.End
        }

        var text: String = _text

        var volume: UInt? = null
        var subpoint: BookMediaConsumeEvent.ReadPoint.Position.Subpoint? = null

        if (previousPoint is BookMediaConsumeEvent.ReadPoint.Position) {
            val value: Pair<UInt?, String>? = text.trimStart().extractBookValue()
            if (value != null) {
                if (previousPoint.volume != null && previousPoint.subpoint == null) {
                    volume = value.first
                    text = value.second
                }
                else if (previousPoint.volume == null && previousPoint.subpoint != null) {
                    subpoint = value.first?.let { BookMediaConsumeEvent.ReadPoint.Position.Subpoint.Chapter(it) }
                    text = value.second
                }
            }
        }

        for (prefix in bookReadVolumePrefixes) {
            if (text.startsWith(prefix)) {
                val value: Pair<UInt?, String> = text.drop(prefix.length).trimStart().extractBookValue() ?: break
                volume = value.first
                text = value.second
                break
            }
        }

        for (prefix in bookReadChapterPrefixes) {
            if (text.startsWith(prefix)) {
                val value: Pair<UInt?, String> = text.drop(prefix.length).trimStart().extractBookValue() ?: break
                subpoint = value.first?.let { BookMediaConsumeEvent.ReadPoint.Position.Subpoint.Chapter(it) }
                text = value.second
                break
            }
        }

        if (subpoint == null) {
            for (prefix in bookReadPagePrefixes) {
                if (text.startsWith(prefix)) {
                    val value: Pair<UInt?, String> = text.drop(prefix.length).trimStart().extractBookValue() ?: break
                    subpoint = value.first?.let { BookMediaConsumeEvent.ReadPoint.Position.Subpoint.Page(it) }
                    break
                }
            }

            if (subpoint == null) {
                subpoint = text.toUIntOrNull()?.let { BookMediaConsumeEvent.ReadPoint.Position.Subpoint.Page(it) }
            }
        }

        if (volume == null && subpoint == null) {
            if (bookReadPagePrefixes.any { it.trim() == text }) {
                subpoint = BookMediaConsumeEvent.ReadPoint.Position.Subpoint.Page(1U)
            }
            else if (bookReadChapterPrefixes.any { it.trim() == text }) {
                subpoint = BookMediaConsumeEvent.ReadPoint.Position.Subpoint.Chapter(1U)
            }
            else if (bookReadVolumePrefixes.any { it.trim() == text }) {
                volume = 1U
            }
            else {
                onAlert(MediaWatchLogParseAlert.UnknownBookReadPoint(extensionId, text, _text))
                return null
            }
        }

        return BookMediaConsumeEvent.ReadPoint.Position(volume, subpoint)
    }

    private fun String.extractBookValue(): Pair<UInt?, String>? {
        toUIntOrNull()?.also {
            return it to ""
        }

        val valueEnd: Int = indexOf(' ')
        if (valueEnd == -1) {
            return null
        }
        return substring(0, valueEnd).toUIntOrNull() to substring(valueEnd).trimStart()
    }

    override fun parseLowercaseGamePlayedRange(text: String, strings: LogFileConverterStrings): GameMediaConsumeEvent.PlayedRange? {
        when (text) {
            "start" -> return GameMediaConsumeEvent.PlayedRange.Start
            "end" -> return GameMediaConsumeEvent.PlayedRange.End
        }

        var text: String = text

        var upTo: Boolean = false
        var unsure: Boolean = false

        for (prefix in mediaDurationRangeToPrefixes) {
            if (text.startsWith(prefix)) {
                upTo = true
                text = text.drop(prefix.length).trimStart()
            }
        }

        for (prefix in unsurePrefixes) {
            if (text.startsWith(prefix)) {
                unsure = true
                text = text.drop(prefix.length).trimStart()
                break
            }
        }

        if (!upTo) {
            for (prefix in mediaDurationRangeToPrefixes) {
                if (text.startsWith(prefix)) {
                    upTo = true
                    text = text.drop(prefix.length).trimStart()
                }
            }
        }

        val duration: Duration? = mediaDurationFormats.firstNotNullOfOrNull { it.parseOrNull(text) }
        if (duration != null) {
            return GameMediaConsumeEvent.PlayedRange.Duration(duration, unsure = unsure)
        }

        var date: LocalDate? = strings.dateFormats.firstNotNullOfOrNull { it.parseOrNull(text) }
        if (date == null) {
            val split: List<String> = text.split('/')
            if (split.size == 2) {
                val month: Int? = split[0].toIntOrNull()
                val day: Int? = split[1].toIntOrNull()
                if (month != null && day != null) {
                    date = LocalDate(0, month, day)
                }
            }
        }

        if (date != null) {
            if (upTo) {
                return GameMediaConsumeEvent.PlayedRange.Days(startDay = null, endDay = date, unsure = unsure)
            }
            else {
                return GameMediaConsumeEvent.PlayedRange.Days(startDay = date, endDay = null, unsure = unsure)
            }
        }

        return null
    }

    override fun movieOrShowProportionWatchRangeToText(range: MovieOrShowMediaConsumeEvent.WatchedRange.Proportion): String =
        when (range) {
            MovieOrShowMediaConsumeEvent.WatchedRange.Proportion.FIRST_HALF -> "first half"
            MovieOrShowMediaConsumeEvent.WatchedRange.Proportion.SECOND_HALF -> "second half"
        }

    override fun mediaRangeValueToText(rangeValue: MediaRangeValue): String =
        when (rangeValue) {
            is MediaRangeValue.Discrete -> rangeValue.value.toString()
            is MediaRangeValue.Continuous -> rangeValue.value.toString()
        }
}