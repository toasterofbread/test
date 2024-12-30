package dev.toastbits.lifelog.application.dbsource.inmemorygit.model

import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.database.LogDataFile
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.database.LogDatabaseData
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference

data class InMemoryGitLogDatabase(
    override val configuration: LogDatabaseConfiguration,
    override val converter: LogFileConverter,
    override val days: Map<LogDate, List<LogEvent>>,
    override val data: Map<LogEntityReference, LogDataFile>,
    val gitCommitHash: String
): LogDatabase {
    override fun _copyWithDays(days: Map<LogDate, List<LogEvent>>): LogDatabase =
        copy(days = days)

    companion object {
        fun fromData(
            data: LogDatabaseData,
            gitCommitHash: String
        ): InMemoryGitLogDatabase =
            InMemoryGitLogDatabase(
                configuration = data.configuration,
                converter = data.converter,
                days = data.days,
                data = data.data,
                gitCommitHash = gitCommitHash
            )
    }
}
