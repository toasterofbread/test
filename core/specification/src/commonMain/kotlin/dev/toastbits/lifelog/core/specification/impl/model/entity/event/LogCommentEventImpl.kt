package dev.toastbits.lifelog.core.specification.impl.model.entity.event

import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.event.LogCommentEvent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.util.StringId

data class LogCommentEventImpl(
    override var content: UserContent?,
    override var inlineComment: UserContent? = null,
    override var aboveComment: UserContent? = null
): LogCommentEvent {
    override fun getIcon(): LogEvent.Icon = LogEvent.Icon.Comment
    override suspend fun getTitle(locale: String): LogDisplayText =
        content?.let { LogDisplayText.OfUserContent(it) } ?: LogDisplayText.OfString("")

    override fun copy(
        content: UserContent?,
        inlineComment: UserContent?,
        aboveComment: UserContent?,
        properties: Map<StringId, LogEntity.Property<*, *>>?
    ): LogEvent =
        copy(
            content = content,
            inlineComment = inlineComment,
            aboveComment = aboveComment
        )
}
