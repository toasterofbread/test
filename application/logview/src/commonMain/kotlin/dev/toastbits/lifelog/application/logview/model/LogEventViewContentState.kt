package dev.toastbits.lifelog.application.logview.model

import dev.toastbits.lifelog.core.specification.model.UserContent

internal sealed interface LogEventViewContentState {
    data class Preview(val content: UserContent): LogEventViewContentState
    data class Edit(val content: String): LogEventViewContentState
}
