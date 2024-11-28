package dev.toastbits.lifelog.core.specification.model.entity.event

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.alert.LogGenerateAlert
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.UserContentParser
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import dev.toastbits.lifelog.core.specification.util.LogStringId
import dev.toastbits.lifelog.core.specification.util.StringId
import kotlin.reflect.KClass

object LogCommentEventType: LogEventType {
    override val name: StringId = LogStringId.EventType.COMMENT
    override val prefixes: List<String> = emptyList()
    override val eventClass: KClass<*> = LogCommentEvent::class

    override fun parseEvent(
        prefixIndex: Int,
        body: String,
        metadata: String?,
        content: UserContent?,
        referenceParser: LogEntityReferenceParser,
        userContentParser: UserContentParser,
        logStrings: LogFileConverterStrings,
        onAlert: (LogParseAlert) -> Unit
    ): LogEvent {
        throw IllegalStateException()
    }

    override fun generateEvent(
        event: LogEvent,
        referenceGenerator: LogEntityReferenceGenerator,
        logStrings: LogFileConverterStrings,
        onAlert: (LogGenerateAlert) -> Unit
    ): LogEventType.EventText {
        throw IllegalStateException()
    }
}
