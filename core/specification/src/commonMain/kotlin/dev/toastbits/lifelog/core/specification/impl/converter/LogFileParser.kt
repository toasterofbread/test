package dev.toastbits.lifelog.core.specification.impl.converter

import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.ParseAlertData
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.converter.alert.SpecificationLogParseAlert
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.UserContentParser
import dev.toastbits.lifelog.core.specification.impl.model.entity.date.LogDateImpl
import dev.toastbits.lifelog.core.specification.impl.model.entity.event.LogCommentEventImpl
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogCommentEvent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEventType
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import kotlinx.datetime.LocalDate
import okio.Path

internal class LogFileParser(
    private val filePath: Path,
    private val strings: LogFileConverterStrings,
    private val eventTypes: List<LogEventType>,
    private val userContentParser: UserContentParser,
    private val referenceParser: LogEntityReferenceParser,
    private val initialDate: LogDate? = null
) {
    private val dateLineParser: DateLineParser =
        object : DateLineParser(strings, userContentParser, referenceParser) {
            override fun onAlert(alert: LogParseAlert) {
                this@LogFileParser.onAlert(alert)
            }
        }

    private lateinit var days: MutableMap<LogDate, MutableList<LogEvent>>
    private lateinit var alerts: MutableList<ParseAlertData>

    private lateinit var iterator: Iterator<String>
    private var currentLineIndex: Int = -1
    private var currentDay: LogDate? = null

    private var lastDayTopLevelComment: IndexedValue<LogCommentEvent>? = null

    private fun hasNext(): Boolean = iterator.hasNext()
    private var queuedLineContent: MutableList<String> = mutableListOf()
    private fun goNext(): String {
        val nextLine: String =
            if (queuedLineContent.isNotEmpty())
                queuedLineContent.joinToString("").also { queuedLineContent.clear() }
            else
                iterator.next().also { currentLineIndex++ }

        return nextLine
    }

    private fun queueLineContent(content: String) {
        if (content.isNotEmpty()) {
            queuedLineContent.add(content)
        }
    }

    private fun onAlert(error: LogParseAlert, line: Int = currentLineIndex) {
        alerts.add(ParseAlertData(error, line.toUInt(), filePath))
    }

    private fun getDayEvents(allowOutsideDay: Boolean = false): MutableList<LogEvent> {
        val day: LogDate? = currentDay
        if (day == null) {
            if (!allowOutsideDay) {
                onAlert(SpecificationLogParseAlert.LogEventOutsideDay)
            }
            return mutableListOf()
        }
        return days.getOrPut(day) { mutableListOf() }
    }

    fun parse(lines: Sequence<String>): LogFileConverter.ParseResult {
        days = mutableMapOf()
        alerts = mutableListOf()
        iterator = lines.iterator()
        currentLineIndex = -1
        currentDay = initialDate

        while (hasNext()) {
            val line: String = goNext().trim()
            parseTopLevelLine(line)
        }

        days.entries.removeAll { it.value.isEmpty() }

        return LogFileConverter.ParseResult(
            days = days,
            alerts = alerts
        )
    }

    private fun parseBlockComment(startLine: String): String {
        val startLineCommentEndIndex: Int = startLine.indexOf(strings.blockCommentEnd)
        if (startLineCommentEndIndex != -1) {
            val commentText: String = startLine.substring(strings.blockCommentStart.length, startLineCommentEndIndex)
            queueLineContent(startLine.drop(startLineCommentEndIndex + strings.blockCommentEnd.length))
            return commentText
        }

        return buildString {
            appendLine(startLine.drop(strings.blockCommentStart.length))

            var terminated: Boolean = false
            while (hasNext()) {
                val line: String = goNext()
                val commentEndIndex: Int = line.indexOf(strings.blockCommentEnd)
                if (commentEndIndex != -1) {
                    append(line.substring(0, commentEndIndex))
                    queueLineContent(line.substring(commentEndIndex + strings.blockCommentEnd.length))
                    terminated = true
                    break
                }
                else {
                    appendLine(line)
                }
            }

            if (!terminated) {
                onAlert(SpecificationLogParseAlert.UnterminatedBlockComment)
            }
        }
    }

    private fun parseTopLevelLine(line: String) {
        if (line.isBlank()) {
            return
        }

        val lineCommentPrefix: String? = strings.commentPrefixes.firstOrNull { line.startsWith(it) }
        if (lineCommentPrefix != null) {
            val commentText: String = line.drop(lineCommentPrefix.length).trimStart()
            onCommentLine(parseUserContent(commentText))
            return
        }

        if (line.startsWith(strings.blockCommentStart)) {
            val commentText: String = parseBlockComment(line)
            onCommentLine(parseUserContent(commentText))
            return
        }

        val dateLine: DateLineParser.DateLineData? = dateLineParser.attemptParseDateLine(line)
        if (dateLine != null) {
            onDateLine(dateLine.date, dateLine.ambiguous, dateLine.inlineComment)
            return
        }

        for (eventType in eventTypes) {
            for ((index, prefix) in eventType.prefixes.withIndex()) {
                if (line.length < prefix.length) {
                    continue
                }

                if (line.take(prefix.length).lowercase() != prefix.lowercase()) {
                    continue
                }

                val eventLine: String = line.drop(prefix.length).trimStart()
                onEventLine(eventType, index, eventLine)

                return
            }
        }

        onAlert(
            SpecificationLogParseAlert.UnmatchedEventFormat(
                line,
                eventTypes.flatMap { it.prefixes }
            )
        )
    }

    private fun parseUserContent(text: String, lineOffset: Int = 0): UserContent {
        val newLines: Int = text.count { it == '\n' }
        return userContentParser.parseUserContent(
            text,
            referenceParser,
            onAlert = { alert, line -> onAlert(alert, currentLineIndex + line - newLines + lineOffset) }
        ).normalised()
    }

    private fun getLastTopLevelCommentIfAdjacent(): UserContent? =
        lastDayTopLevelComment?.let { lastComment ->
            if (lastComment.index != currentLineIndex - 1) {
                return@let null
            }

            if (currentDay != null) {
                val events: MutableList<LogEvent> = getDayEvents()
                check(events.contains(lastComment.value))
                events.remove(lastComment.value)
            }

            return@let lastDayTopLevelComment!!.value.content
        }

    private fun onDateLine(date: LocalDate?, ambiguous: Boolean, inlineComment: UserContent?) {
        if (date == null) {
            onAlert(SpecificationLogParseAlert.MissingDateError)
            return
        }

        currentDay = LogDateImpl(
            date = date,
            ambiguous = ambiguous,
            inlineComment = inlineComment,
            aboveComment = getLastTopLevelCommentIfAdjacent()
        )
        getDayEvents()
    }

    private fun onEventLine(eventType: LogEventType, eventPrefixIndex: Int, fullLine: String) {
        var line: String = fullLine

        val body: String
        val metadata: String?
        val inlineComment: UserContent?
        val contentLines: MutableList<String> = mutableListOf()

        val aboveComment: UserContent? = getLastTopLevelCommentIfAdjacent()

        val metadataStart: Int = line.indexOf(strings.eventMetadataStart)
        val contentStart: Int = line.indexOf(strings.eventContentStart)

        val (commentPrefix: String, commentStart: Int?) =
            strings.commentPrefixes
                .firstNotNullOfOrNull { commentPrefix ->
                    commentPrefix to (
                        line.indexOf(commentPrefix, contentStart + 1).takeIf { it != -1 } ?: return@firstNotNullOfOrNull null
                    )
                } ?: Pair("", null)

        if (commentStart != null) {
            inlineComment = parseUserContent(line.substring(commentStart + commentPrefix.length).trimStart())
            line = line.substring(0, commentStart)
        }
        else {
            inlineComment = null
        }

        if (metadataStart != -1 && (metadataStart < contentStart || contentStart == -1)) {
            val metadataEnd: Int = line.indexOf(strings.eventMetadataEnd, metadataStart)

            if (metadataEnd == -1) {
                onAlert(SpecificationLogParseAlert.UnterminatedEventMetadata)
                return
            }

            body = line.substring(0, metadataStart).trim()
            metadata = line.substring(metadataStart + strings.eventMetadataStart.length, metadataEnd).trim()
        }
        else {
            metadata = null

            if (contentStart == -1) {
                body = line
            }
            else {
                body = line.substring(0, contentStart)
            }
        }

        if (contentStart != -1) {
            var contentEnd: Int = line.indexOf(strings.eventContentEnd, contentStart)

            if (contentStart + 1 < line.length) {
                if (contentEnd != -1) {
                    // Content ends on this line
                    contentLines.add(line.substring(contentStart + 1, contentEnd))
                    return
                }
                else {
                    contentLines.add(line.substring(contentStart + 1))
                }
            }

            while (hasNext()) {
                val contentLine: String = goNext()
                contentEnd = contentLine.indexOf(strings.eventContentEnd)
                if (contentEnd != -1) {
                    contentLines.add(contentLine.substring(0, contentEnd))
                    break
                }
                else {
                    contentLines.add(contentLine)
                }
            }

            if (contentEnd == -1) {
                onAlert(SpecificationLogParseAlert.EventContentNotTerminated)
            }
        }

        val content: UserContent? =
            parseUserContent(contentLines.joinToString("\n").trimIndent(), -1)
                .takeIf { !it.isEmpty() }

        val event: LogEvent =
            eventType
                .parseEvent(eventPrefixIndex, body, metadata, content, referenceParser, userContentParser, strings, ::onAlert)
                .copy(
                    inlineComment = inlineComment,
                    aboveComment = aboveComment
                )

        getDayEvents().add(event)
    }

    private fun onCommentLine(content: UserContent) {
        val comment: LogCommentEventImpl = LogCommentEventImpl(content)
        lastDayTopLevelComment = IndexedValue(currentLineIndex, comment)

        getDayEvents(allowOutsideDay = true).add(comment)
    }
}
