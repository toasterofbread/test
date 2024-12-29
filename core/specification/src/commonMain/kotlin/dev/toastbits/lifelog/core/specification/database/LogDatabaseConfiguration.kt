package dev.toastbits.lifelog.core.specification.database

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.extension.ExtensionRegistry
import dev.toastbits.lifelog.core.specification.model.entity.event.LogCommentEventType
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEventType

interface LogDatabaseConfiguration {
    val extensionRegistry: ExtensionRegistry
    val splitStrategy: LogFileSplitStrategy
    val strings: LogFileConverterStrings

    val defaultEvent: LogEvent

    companion object {
        val BUILT_IN_EVENT_TYPES: List<LogEventType> =
            listOf(
                LogCommentEventType
            )
    }
}

fun LogDatabaseConfiguration.findTypeOfEvent(event: LogEvent): LogEventType {
    for (type in LogDatabaseConfiguration.BUILT_IN_EVENT_TYPES) {
        if (type.eventClass.isInstance(event)) {
            return type
        }
    }

    for (extension in extensionRegistry.getAllExtensions()) {
        for (type in extension.extraEventTypes) {
            if (type.eventClass.isInstance(event)) {
                return type
            }
        }
    }

    throw RuntimeException("Type for event '${event::class}' not found")
}
