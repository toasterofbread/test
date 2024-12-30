package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step

import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.model.LogDatabaseParseResult
import dev.toastbits.lifelog.core.specification.database.LogDatabase

internal class LoadStepLoadOnline<T: LogDatabase>: LoadStep<LogDatabaseParseResult<T>, T> {
    override suspend fun execute(accessor: DatabaseAccessor<T>, onProgress: (DatabaseAccessor.LoadProgress) -> Unit): LoadStep.ExecuteResult<LogDatabaseParseResult<T>> =
        accessor.loadOnlineDatabase(onProgress)
            .fold(
                onSuccess = {
                    LoadStep.ExecuteResult.Done(it)
                },
                onFailure = {
                    LoadStep.ExecuteResult.ExceptionThrown(it)
                }
            )
}
