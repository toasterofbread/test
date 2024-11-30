package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step

import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.model.LogDatabaseParseResult

internal data object LoadStepLoadOnline: LoadStep<LogDatabaseParseResult> {
    override suspend fun execute(accessor: DatabaseAccessor, onProgress: (DatabaseAccessor.LoadProgress) -> Unit): LoadStep.ExecuteResult<LogDatabaseParseResult> =
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
