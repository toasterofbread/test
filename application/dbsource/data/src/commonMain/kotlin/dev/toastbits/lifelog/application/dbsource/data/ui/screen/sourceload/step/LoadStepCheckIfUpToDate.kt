package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step

import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStep.ExecuteResult
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.OfflineDatabaseAccessor

internal data class LoadStepCheckIfUpToDate<R>(val accessor: OfflineDatabaseAccessor): LoadStep<R> {
    override suspend fun execute(
        accessor: DatabaseAccessor,
        onProgress: (DatabaseAccessor.LoadProgress) -> Unit
    ): ExecuteResult<R> {
        TODO("Not yet implemented")
    }
}
