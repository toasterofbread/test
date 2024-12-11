package dev.toastbits.lifelog.core.specification.impl.model.entity.event

import dev.toastbits.composekit.util.model.Locale
import dev.toastbits.lifelog.core.specification.localisation.LogStringId
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.event.LogCommentEvent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.string.StringId

data class LogCommentEventImpl(
    override var content: UserContent?,
    override var inlineComment: UserContent? = null,
    override var aboveComment: UserContent? = null
): LogCommentEvent {
    override val typeName: StringId get() = LogStringId.LogCommentEvent
    override val typeVerb: StringId get() = LogStringId.LogCommentEventVerb

    override fun getIcon(): LogEvent.Icon = LogEvent.Icon.Comment

    override suspend fun getPreview(locale: Locale): LogDisplayText =
        content?.let { LogDisplayText.OfUserContent(it) } ?: LogDisplayText.OfString("")

    override suspend fun getTitle(locale: Locale): LogDisplayText? = null

    override fun copy(
        inlineComment: UserContent?,
        aboveComment: UserContent?
    ): LogEvent =
        copy(
            content = content,
            inlineComment = inlineComment,
            aboveComment = aboveComment
        )

    override fun copy(
        content: UserContent?
    ): LogEvent =
        copy(
            content = content,
            inlineComment = inlineComment,
            aboveComment = aboveComment
        )
}
