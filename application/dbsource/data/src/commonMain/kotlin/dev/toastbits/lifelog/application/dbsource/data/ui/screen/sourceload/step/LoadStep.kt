package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step

import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import kotlinx.coroutines.CancellationException

internal sealed interface LoadStep<R> {
    suspend fun execute(accessor: DatabaseAccessor, onProgress: (DatabaseAccessor.LoadProgress) -> Unit): ExecuteResult<R>

    sealed interface ExecuteResult<R> {
        data class Done<R>(val result: R): ExecuteResult<R>
        data class NextStep<R>(val nextStep: LoadStep<R>): ExecuteResult<R>
        data class ExceptionThrown<R>(val exception: Throwable): ExecuteResult<R> {
            init {
                require(exception !is CancellationException)
            }
        }
    }
}
