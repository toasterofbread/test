package dev.toastbits.lifelog.core.specification.model.entity.event

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.alert.LogGenerateAlert
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.UserContentParser
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import dev.toastbits.lifelog.core.specification.util.StringId
import kotlin.reflect.KClass

interface LogEventType {
    val name: StringId
    val prefixes: List<String>
    val eventClass: KClass<*>

    fun parseEvent(
        prefixIndex: Int,
        body: String,
        metadata: String?,
        content: UserContent?,
        referenceParser: LogEntityReferenceParser,
        userContentParser: UserContentParser,
        logStrings: LogFileConverterStrings,
        onAlert: (LogParseAlert) -> Unit
    ): LogEvent

    fun generateEvent(
        event: LogEvent,
        referenceGenerator: LogEntityReferenceGenerator,
        logStrings: LogFileConverterStrings,
        onAlert: (LogGenerateAlert) -> Unit
    ): EventText

    data class EventText(
        val prefix: String,
        val body: String,
        val metadata: String?
    )
}
