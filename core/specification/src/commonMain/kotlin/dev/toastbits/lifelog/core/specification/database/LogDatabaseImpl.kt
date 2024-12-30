package dev.toastbits.lifelog.core.specification.database

import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference

interface LogDatabase {
    val configuration: LogDatabaseConfiguration
    val converter: LogFileConverter
    val days: Map<LogDate, List<LogEvent>>
    val data: Map<LogEntityReference, LogDataFile>

    fun _copyWithDays(days: Map<LogDate, List<LogEvent>>): LogDatabase
}

@Suppress("UNCHECKED_CAST")
fun <T: LogDatabase> T.copyWithDays(days: Map<LogDate, List<LogEvent>>): T =
    _copyWithDays(days) as T

class LogDatabaseData(
    val configuration: LogDatabaseConfiguration,
    val converter: LogFileConverter,
    val days: Map<LogDate, List<LogEvent>>,
    val data: Map<LogEntityReference, LogDataFile>
)

sealed interface LogDataFile {
    class Lines(val lines: List<String>): LogDataFile
    class Bytes(val bytes: ByteArray, val range: IntRange = bytes.indices): LogDataFile
}
