package dev.toastbits.lifelog.application.logview.model

import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent

data class LogEventReference(val date: LogDate, val logIndex: Int)

fun LogDatabase.getOrNull(reference: LogEventReference): LogEvent? =
    days[reference.date]?.get(reference.logIndex)

operator fun LogDatabase.get(reference: LogEventReference): LogEvent =
    getOrNull(reference)!!
