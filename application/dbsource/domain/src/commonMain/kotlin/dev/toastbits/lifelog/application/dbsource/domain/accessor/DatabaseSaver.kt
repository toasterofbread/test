package dev.toastbits.lifelog.application.dbsource.domain.accessor

import dev.toastbits.kogit.memory.handler.GitCommitGenerator
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import dev.toastbits.lifelog.core.specification.database.LogDatabase

interface DatabaseSaver {
    suspend fun saveOnlineDatabase(
        database: LogDatabase,
        message: String,
        author: GitCommitGenerator.UserInfo,
        committer: GitCommitGenerator.UserInfo,
        onProgress: (DatabaseAccessor.LoadProgress) -> Unit
    ): Result<SaveResult>

    sealed interface SaveResult {
        val alerts: List<Alert>

        data class Success(val newDatabase: LogDatabase, override val alerts: List<Alert>): SaveResult
        data class Failure(override val alerts: List<Alert>): SaveResult
    }
}
