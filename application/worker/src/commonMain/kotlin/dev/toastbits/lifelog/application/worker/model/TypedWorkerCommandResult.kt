package dev.toastbits.lifelog.application.worker.model

import dev.toastbits.lifelog.application.worker.command.WorkerCommandResponse
import kotlinx.serialization.Serializable

sealed interface TypedWorkerCommandResult<R: WorkerCommandResponse> {
    @Serializable
    data class Success<R: WorkerCommandResponse>(val response: R): TypedWorkerCommandResult<R>
    @Serializable
    data class Exception<R: WorkerCommandResponse>(val stackTraceString: String, val exceptionClass: String): TypedWorkerCommandResult<R>
}

fun <R: WorkerCommandResponse> TypedWorkerCommandResult<R>.getOrThrow(): R =
    when (this) {
        is TypedWorkerCommandResult.Success -> response
        is TypedWorkerCommandResult.Exception -> throw RuntimeException("$exceptionClass\n$stackTraceString")
    }

@Suppress("UNCHECKED_CAST")
fun <R: WorkerCommandResponse> WorkerCommandResult.cast(): TypedWorkerCommandResult<R> =
    when (this) {
        is WorkerCommandResult.Success -> TypedWorkerCommandResult.Success(response as R)
        is WorkerCommandResult.Progress -> throw IllegalStateException(this.toString())
        is WorkerCommandResult.Failure -> TypedWorkerCommandResult.Exception(exception.stackTraceString, exception.exceptionClass)
    }
