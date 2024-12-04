package dev.toastbits.lifelog.core.specification.model.entity.event

import dev.toastbits.lifelog.core.specification.extension.ExtensionId

interface LogCommentEvent: LogEvent {
    override val extensionId: ExtensionId? get() = null
}
