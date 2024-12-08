package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step

import dev.toastbits.kogit.memory.handler.GitCommitGenerator.UserInfo
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.SaveResult
import dev.toastbits.lifelog.core.specification.database.LogDatabase

internal data class LoadStepSaveOnline(
    val database: LogDatabase,
    val message: String,
    val author: UserInfo,
    val committer: UserInfo
): LoadStep<SaveResult> {
    override suspend fun execute(accessor: DatabaseAccessor, onProgress: (DatabaseAccessor.LoadProgress) -> Unit): LoadStep.ExecuteResult<SaveResult> =
        accessor.saveOnlineDatabase(
            database,
            message,
            author,
            committer,
            onProgress
        )
            .fold(
                onSuccess = {
                    LoadStep.ExecuteResult.Done(it)
                },
                onFailure = {
                    LoadStep.ExecuteResult.ExceptionThrown(it)
                }
            )
}
