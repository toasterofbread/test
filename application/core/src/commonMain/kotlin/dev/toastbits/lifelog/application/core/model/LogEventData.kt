package dev.toastbits.lifelog.application.core.model

import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent

data class LogEventData(
    val event: LogEvent,
    val date: LogDate,
    val configuration: LogDatabaseConfiguration
): LogEvent by event, LogDatabaseConfiguration by configuration

fun LogEventData.formatDate(): String =
    configuration.strings.preferredDateFormat.format(date.date)
