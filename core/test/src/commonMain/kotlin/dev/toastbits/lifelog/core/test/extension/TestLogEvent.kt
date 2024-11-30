package dev.toastbits.lifelog.core.test.extension

import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.util.StringId

data class TestLogEvent(
    val reference: LogEntityReference.InMetadata,
    override var inlineComment: UserContent? = null,
    override var content: UserContent? = null,
    override var aboveComment: UserContent? = null
) : LogEvent {
    override fun getIcon(): LogEvent.Icon = LogEvent.Icon.Comment
    override suspend fun getTitle(locale: String): LogDisplayText = LogDisplayText.OfString("")
    override fun copy(
        content: UserContent?,
        inlineComment: UserContent?,
        aboveComment: UserContent?,
        properties: Map<StringId, LogEntity.Property<*, *>>?
    ): LogEvent =
        copy(
            reference = reference,
            inlineComment = inlineComment,
            content = content,
            aboveComment = aboveComment
        )
}
