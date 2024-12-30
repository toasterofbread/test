package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step

import dev.toastbits.kogit.memory.handler.GitCommitGenerator.UserInfo
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseSaver.SaveResult
import dev.toastbits.lifelog.core.specification.database.LogDatabase

internal data class LoadStepSaveOnline<T: LogDatabase>(
    val database: T,
    val message: String,
    val author: UserInfo,
    val committer: UserInfo
): LoadStep<SaveResult<T>, T> {
    override suspend fun execute(
        accessor: DatabaseAccessor<T>,
        onProgress: (DatabaseAccessor.LoadProgress) -> Unit,
    ): LoadStep.ExecuteResult<SaveResult<T>> =
        accessor.saver.saveOnlineDatabase(
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
