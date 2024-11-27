package dev.toastbits.lifelog.core.test.extension

import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference

class TestLogEvent(
    val reference: LogEntityReference.InMetadata,
    override var inlineComment: UserContent? = null,
    override var content: UserContent? = null,
    override var aboveComment: UserContent? = null
) : LogEvent {
    override fun getIcon(): LogEvent.Icon = LogEvent.Icon.Comment
}
