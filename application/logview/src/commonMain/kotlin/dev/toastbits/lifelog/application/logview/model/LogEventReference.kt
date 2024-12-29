package dev.toastbits.lifelog.application.logview.model

import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent

data class LogEventReference(val date: LogDate, val logIndex: Int): Comparable<LogEventReference> {
    override fun compareTo(other: LogEventReference): Int =
        date.date.compareTo(other.date.date)
            .takeIf { it != 0 }
            ?: logIndex.compareTo(other.logIndex)
}

fun LogDatabase.getOrNull(reference: LogEventReference): LogEvent? =
    days[reference.date]?.getOrNull(reference.logIndex)

operator fun LogDatabase.get(reference: LogEventReference): LogEvent =
    getOrNull(reference) ?: throw NullPointerException("Reference $reference not found in database")
