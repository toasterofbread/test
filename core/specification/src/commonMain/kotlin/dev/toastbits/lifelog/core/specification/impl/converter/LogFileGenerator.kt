package dev.toastbits.lifelog.core.specification.impl.converter

import dev.toastbits.lifelog.core.specification.converter.GenerateAlertData
import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.alert.LogGenerateAlert
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.UserContentGenerator
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogCommentEvent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEventType
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import kotlinx.datetime.LocalDate

internal class LogFileGenerator(
    private val strings: LogFileConverterStrings,
    private val eventTypes: List<LogEventType>,
    private val userContentGenerator: UserContentGenerator,
    private val referenceGeneratorProvider: (LocalDate) -> LogEntityReferenceGenerator
) {
    private lateinit var lines: MutableList<String>
    private lateinit var alerts: MutableList<GenerateAlertData>

    private var currentLineIndex: Int = 0
    private var currentDate: LogDate? = null

    private fun onAlert(error: LogGenerateAlert, line: Int = currentLineIndex) {
        alerts.add(GenerateAlertData(error, line.toUInt(), null))
    }

    private fun UserContent.toText(): String =
        userContentGenerator.generateUserContent(this, referenceGeneratorProvider(currentDate!!.date), ::onAlert)

    private fun addLine(content: String, entity: LogEntity? = null) {
        check(!content.contains('\n'))

        entity?.aboveComment?.also { aboveComment ->
            lines.add(strings.commentPrefix + aboveComment.toText())
            currentLineIndex++
        }

        val lineContent: String =
            entity?.inlineComment?.let { inlineComment ->
                content + ' ' + strings.commentPrefix + inlineComment.toText()
            } ?: content

        lines.add(lineContent)

        currentLineIndex++
    }

    fun generate(days: Map<LogDate, List<LogEvent>>): LogFileConverter.GenerateResult {
        lines = mutableListOf()
        alerts = mutableListOf()
        currentLineIndex = 0
        currentDate = null

        val sortedDays: List<LogDate> = days.keys.sortedBy { it.date }
        for (date in sortedDays) {
            onDate(date)

            val events: List<LogEvent> = days[date]!!
            for (event in events) {
                if (event is LogCommentEvent) {
                    onComment(event)
                }
                else {
                    onEvent(event)
                }
            }
        }

        return LogFileConverter.GenerateResult(
            lines = lines,
            alerts = alerts
        )
    }

    private fun onDate(date: LogDate) {
        currentDate = date

        val formattedDateString: String = strings.preferredDateFormat.format(date.date)
        val dateLine: String =
            buildString {
                append(strings.datePrefix)

                if (date.ambiguous) {
                    append(strings.ambiguousDatePrefix)
                    append(formattedDateString.replaceFirstChar { it.lowercase() })
                }
                else {
                    append(formattedDateString)
                }
            }

        addLine(dateLine, date)
        addLine("")
    }

    private fun onComment(comment: LogCommentEvent) {
        val referenceGenerator: LogEntityReferenceGenerator = referenceGeneratorProvider(currentDate!!.date)
        val commentTextLines: List<String> = comment.content?.let { userContentGenerator.generateUserContent(it, referenceGenerator, ::onAlert) }.orEmpty().split('\n')

        // Single line comment
        if (commentTextLines.size <= 1) {
            addLine(
                buildString {
                    append(strings.commentPrefix)
                    if (commentTextLines.isNotEmpty()) {
                        append(commentTextLines.single())
                    }
                },
                comment
            )
        }
        // Block comment
        else {
            addLine(strings.blockCommentStart + commentTextLines.first(), comment)
            for (lineIndex in 1 until commentTextLines.size - 1) {
                addLine(commentTextLines[lineIndex])
            }
            addLine(commentTextLines.last() + strings.blockCommentEnd)
        }

        addLine("")
    }

    private fun onEvent(event: LogEvent) {
        val eventType: LogEventType? = eventTypes.firstOrNull { it.eventClass.isInstance(event) }
        checkNotNull(eventType) { "No event type could generate for event $event (${event::class})" }

        val referenceGenerator: LogEntityReferenceGenerator = referenceGeneratorProvider(currentDate!!.date)

        val eventText: LogEventType.EventText = eventType.generateEvent(event, referenceGenerator, strings, ::onAlert)
        addLine(
            buildString {
                append(eventText.prefix)
                append(eventText.body)
                if (eventText.metadata != null) {
                    append(" (")
                    append(eventText.metadata)
                    append(')')
                }

                if (event.content != null) {
                    append(" {")
                }
            },
            event
        )

        event.content?.normalised()?.takeIf { !it.isBlank() }?.also { content ->
            addLine("")
            val contentTextLines: List<String> = userContentGenerator.generateUserContent(content, referenceGenerator, ::onAlert).split('\n')
            for (line in contentTextLines) {
                addLine(strings.contentIndentation + line)
            }
            addLine("")
            addLine("}")
        }
        addLine("")
    }
}
