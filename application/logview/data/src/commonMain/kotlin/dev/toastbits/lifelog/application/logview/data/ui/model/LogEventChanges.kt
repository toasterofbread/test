package dev.toastbits.lifelog.application.logview.data.ui.model

import dev.toastbits.lifelog.core.specification.model.UserContent

data class LogEventChanges(
    val content: UserContent? = null
) {
    fun hasChanges(): Boolean =
        this != EMPTY

    companion object {
        val EMPTY: LogEventChanges = LogEventChanges()
    }
}