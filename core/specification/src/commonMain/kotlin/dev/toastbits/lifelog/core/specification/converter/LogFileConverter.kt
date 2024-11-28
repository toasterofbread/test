package dev.toastbits.lifelog.core.specification.converter

import dev.toastbits.lifelog.core.specification.converter.alert.LogConvertAlert
import dev.toastbits.lifelog.core.specification.converter.alert.LogGenerateAlert
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.UserContentGenerator
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.UserContentParser
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import kotlinx.datetime.LocalDate

interface LogFileConverter {
    val referenceParser: LogEntityReferenceParser
    val referenceGeneratorProvider: (LocalDate) -> LogEntityReferenceGenerator
    val userContentParser: UserContentParser
    val userContentGenerator: UserContentGenerator

    fun parseLogFile(lines: Sequence<String>, initialDate: LogDate? = null): ParseResult
    fun generateLogFile(days: Map<LogDate, List<LogEvent>>): GenerateResult

    data class ParseResult(
        val days: Map<LogDate, List<LogEvent>>,
        val alerts: List<ParseAlertData>
    )

    data class GenerateResult(
        val lines: List<String>,
        val alerts: List<GenerateAlertData>
    )

    data class AlertOnLine<T: LogConvertAlert>(
        val alert: T,
        val lineIndex: UInt?,
        val filePath: String?
    )
}

typealias ParseAlertData = LogFileConverter.AlertOnLine<LogParseAlert>
typealias GenerateAlertData = LogFileConverter.AlertOnLine<LogGenerateAlert>
