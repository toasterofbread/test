package dev.toastbits.lifelog.application.worker

import dev.toastbits.lifelog.application.worker.command.WorkerCommand
import dev.toastbits.lifelog.application.worker.command.WorkerCommandProgress
import dev.toastbits.lifelog.application.worker.command.WorkerCommandResponse
import dev.toastbits.lifelog.application.worker.mapper.WorkerExecutionContext
import dev.toastbits.lifelog.application.worker.model.TypedWorkerCommandResult
import dev.toastbits.lifelog.application.worker.model.cast
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

actual class WorkerClient(
    private val context: WorkerExecutionContext
) {
    private val mutex: Mutex = Mutex()

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    actual suspend inline fun <reified R : WorkerCommandResponse> executeCommand(
        command: WorkerCommand,
        noinline onProgress: (WorkerCommandProgress) -> Unit
    ): Result<TypedWorkerCommandResult<R>> = withContext(context.defaultDispatcher) {
        if (!mutex.tryLock()) {
            return@withContext Result.failure(ConcurrentModificationException("WorkerClient does not support simultaneous commands ($command)"))
        }

        val result: TypedWorkerCommandResult<R> =
            command.execute(context, onProgress).cast()

        mutex.unlock()

        return@withContext Result.success(result)
    }
}
