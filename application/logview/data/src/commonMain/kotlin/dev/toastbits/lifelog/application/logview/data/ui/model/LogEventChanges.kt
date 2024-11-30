package dev.toastbits.lifelog.application.logview.data.ui.model

import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent

data class LogEventChanges(
    val content: UserContent? = null
) {
    fun hasChanges(): Boolean =
        this != EMPTY

    fun applyTo(event: LogEvent): LogEvent =
        event.copy(
            content = content ?: event.content,
            inlineComment = event.inlineComment, // TODO
            aboveComment = event.aboveComment, // TODO
            properties = null
        )

    companion object {
        val EMPTY: LogEventChanges = LogEventChanges()
    }
}