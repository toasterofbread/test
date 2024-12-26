package dev.toastbits.lifelog.application.logview.manager

import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import kotlin.time.Duration

class LogDatabaseQueuedChanges(
    val delay: Duration,
    val loadChanges: suspend (currentChanges: LogEntityChanges<LogEvent>) -> LogEntityChanges<LogEvent>
)
