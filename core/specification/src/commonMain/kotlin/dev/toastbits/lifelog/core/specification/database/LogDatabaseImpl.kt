package dev.toastbits.lifelog.core.specification.database

import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference

data class LogDatabase(
    val configuration: LogDatabaseConfiguration,
    val days: Map<LogDate, List<LogEvent>> = emptyMap(),
    val data: Map<LogEntityReference, LogDataFile> = emptyMap(),
    val converter: LogFileConverter
)

sealed interface LogDataFile {
    class Lines(val lines: List<String>): LogDataFile
    class Bytes(val bytes: ByteArray, val range: IntRange = bytes.indices): LogDataFile
}
