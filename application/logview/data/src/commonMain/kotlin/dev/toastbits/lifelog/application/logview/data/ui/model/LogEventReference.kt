package dev.toastbits.lifelog.application.logview.data.ui.model

import dev.toastbits.lifelog.application.core.model.LogEventData
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate

data class LogEventReference(val date: LogDate, val logIndex: Int)

operator fun LogDatabase.get(reference: LogEventReference): LogEventData =
    LogEventData(days[reference.date]!![reference.logIndex], reference.date, configuration)
