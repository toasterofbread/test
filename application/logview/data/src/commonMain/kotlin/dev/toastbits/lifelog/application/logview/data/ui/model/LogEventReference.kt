package dev.toastbits.lifelog.application.logview.data.ui.model

import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent

data class LogEventReference(val date: LogDate, val logIndex: Int)

operator fun LogDatabase.get(key: LogEventReference): LogEvent =
    days[key.date]!![key.logIndex]
