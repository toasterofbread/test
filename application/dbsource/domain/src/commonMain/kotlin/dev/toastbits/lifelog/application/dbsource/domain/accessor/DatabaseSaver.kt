package dev.toastbits.lifelog.application.dbsource.domain.accessor

import dev.toastbits.kogit.memory.handler.GitCommitGenerator
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import dev.toastbits.lifelog.core.specification.database.LogDatabase

interface DatabaseSaver<T: LogDatabase> {
    suspend fun saveOnlineDatabase(
        database: T,
        message: String,
        author: GitCommitGenerator.UserInfo,
        committer: GitCommitGenerator.UserInfo,
        onProgress: (DatabaseAccessor.LoadProgress) -> Unit
    ): Result<SaveResult<T>>

    sealed interface SaveResult<T: LogDatabase> {
        val alerts: List<Alert>

        data class Success<T: LogDatabase>(val newDatabase: T, override val alerts: List<Alert>): SaveResult<T>
        data class Failure<T: LogDatabase>(override val alerts: List<Alert>): SaveResult<T>
    }
}
