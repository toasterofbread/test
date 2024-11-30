package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step

import dev.toastbits.kogit.memory.handler.GitCommitGenerator.UserInfo
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.core.specification.database.LogDatabase

internal data class LoadStepSaveOnline(
    val database: LogDatabase,
    val message: String,
    val author: UserInfo,
    val committer: UserInfo
): LoadStep<Unit> {
    override suspend fun execute(accessor: DatabaseAccessor, onProgress: (DatabaseAccessor.LoadProgress) -> Unit): LoadStep.ExecuteResult<Unit> =
        accessor.saveOnlineDatabase(
            database,
            message,
            author,
            committer,
            onProgress
        )
            .fold(
                onSuccess = {
                    LoadStep.ExecuteResult.Done(Unit)
                },
                onFailure = {
                    LoadStep.ExecuteResult.ExceptionThrown(it)
                }
            )
}
