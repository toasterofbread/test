package dev.toastbits.lifelog.core.specification.impl.converter

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.converter.alert.SpecificationLogParseAlert
import dev.toastbits.lifelog.core.specification.converter.parseOrNull
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.UserContentParser
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import kotlinx.datetime.LocalDate

abstract class DateLineParser(
    private val strings: LogFileConverterStrings,
    private val userContentParser: UserContentParser,
    private val referenceParser: LogEntityReferenceParser
) {
    data class DateLineData(val date: LocalDate?, val ambiguous: Boolean, val inlineComment: UserContent?)

    abstract fun onAlert(alert: LogParseAlert)

    fun attemptParseDateLine(line: String): DateLineData? {
        if (!line.startsWith(strings.datePrefix)) {
            return null
        }

        var (dateText: String, inlineComment: UserContent?) = line.drop(strings.datePrefix.length).extractComment()
        var ambiguous: Boolean = false

        if (dateText.lowercase().startsWith(strings.ambiguousDatePrefix.lowercase())) {
            ambiguous = true
            dateText = dateText.drop(strings.ambiguousDatePrefix.length).trimStart()
        }

        val date: LocalDate? = parseDate(dateText)
        return DateLineData(date, ambiguous, inlineComment)
    }

    private fun parseDate(text: String): LocalDate? {
        for (dateFormat in strings.dateFormats) {
            val date: LocalDate = dateFormat.parseOrNull(text) ?: continue
            return date
        }

        onAlert(SpecificationLogParseAlert.NoMatchingDateFormat(text))
        return null
    }

    private fun parseUserContent(text: String): UserContent =
        userContentParser.parseUserContent(
            text,
            referenceParser,
            onAlert = { alert, _ -> onAlert(alert) }
        ).normalised()

    private fun String.extractComment(): Pair<String, UserContent?> {
        val (commentPrefix: String, commentStart: Int?) =
            strings.commentPrefixes
                .firstNotNullOfOrNull { commentPrefix ->
                    commentPrefix to (indexOf(commentPrefix).takeIf { it != -1 } ?: return@firstNotNullOfOrNull null)
                } ?: Pair("", null)

        if (commentStart == null) {
            return this.trim() to null
        }

        val comment: String = drop(commentStart + commentPrefix.length).trim()
        return substring(0, commentStart).trim() to parseUserContent(comment)
    }
}
