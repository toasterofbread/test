package dev.toastbits.lifelog.application.worker

import dev.toastbits.lifelog.application.worker.command.WorkerCommand
import dev.toastbits.lifelog.application.worker.command.WorkerCommandCancelCurrent
import dev.toastbits.lifelog.application.worker.command.WorkerCommandProgress
import dev.toastbits.lifelog.application.worker.command.WorkerCommandResponse
import dev.toastbits.lifelog.application.worker.model.TypedWorkerCommandResult
import dev.toastbits.lifelog.application.worker.model.WorkerCommandResult
import dev.toastbits.lifelog.application.worker.model.cast
import dev.toastbits.lifelog.application.worker.model.toPotentialError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.encodeToString
import org.w3c.dom.ErrorEvent
import org.w3c.dom.MessageEvent
import org.w3c.dom.Worker

actual class WorkerClient {
    private val worker: Worker = createWorker()
    private val mutex: Mutex = Mutex()
    private val workerEncoder: WorkerEncoder = WorkerEncoder()
    private val resultChannel: Channel<WorkerCommandResult> = Channel()
    private var pendingErrorEvents: MutableList<ErrorEvent> = mutableListOf()

    private var firstMessageSkipped: Boolean = false

    private fun msg(message: String): String = "Worker (client): $message"
    private fun log(message: String) = println(msg(message))

    private fun popPendingMessages(): List<WorkerCommandProgress> {
        if (pendingErrorEvents.isNotEmpty()) {
            val errors: MutableList<ErrorEvent> = pendingErrorEvents
            pendingErrorEvents = mutableListOf()

            return errors.map { error ->
                error.toPotentialError()
            }
        }
        return emptyList()
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    actual suspend inline fun <reified R: WorkerCommandResponse> executeCommand(
        command: WorkerCommand,
        noinline onProgress: (WorkerCommandProgress) -> Unit
    ): Result<TypedWorkerCommandResult<R>> {
        try {
            if (!firstMessageSkipped) {
                onProgress(WorkerCommandProgress.WaitingForWorker)
                worker

                while (!firstMessageSkipped) {
                    delay(10)
                    popPendingMessages().forEach(onProgress)
                }

                onProgress(WorkerCommandProgress.WorkerStarted)
            }

            if (!mutex.tryLock()) {
                return Result.failure(ConcurrentModificationException("WorkerClient does not support simultaneous commands ($command)"))
            }

            try {
                val serialisedCommand: String =
                    try {
                        workerJson.encodeToString(command)
                    }
                    catch (e: Throwable) {
                        return Result.failure(RuntimeException(msg("Serialising command '$command' failed"), e))
                    }

                workerEncoder.post(worker, serialisedCommand)

                while (true) {
                    popPendingMessages().forEach(onProgress)

                    val result: WorkerCommandResult =
                        withTimeoutOrNull(500) { resultChannel.receive() }
                        ?: continue

                    when (result) {
                        is WorkerCommandResult.Success ->
                            when (val response: WorkerCommandResponse = result.response) {
                                is R -> return Result.success(result.cast())
                                else -> return Result.failure(RuntimeException(msg("Received response of unknown type '${response::class}' while waiting for '${R::class}' ($response)")))
                            }
                        is WorkerCommandResult.Progress -> onProgress(result.progress)
                        is WorkerCommandResult.Failure -> return Result.success(result.cast())
                    }
                }
            }
            finally {
                mutex.unlock()
            }
        }
        catch (e: CancellationException) {
            log("Command cancelled, sending WorkerCommandCancelCurrent")
            workerEncoder.post(worker, workerJson.encodeToString<WorkerCommand>(WorkerCommandCancelCurrent))
            throw e
        }
    }

    private fun handleMessage(message: MessageEvent) {
        if (!firstMessageSkipped) {
            log("Skipping first message (${message.data})")
            firstMessageSkipped = true
            return
        }

        val response: WorkerCommandResult =
            try {
                workerEncoder.decode(message)
            }
            catch (e: Throwable) {
                RuntimeException(msg("Message '${message.data}' could not be deserialised"), e).printStackTrace()
                return
            }

        val result: ChannelResult<Unit> = resultChannel.trySend(response)
        result.exceptionOrNull()?.also { exception ->
            RuntimeException(msg("Message '$message' was not consumed"), exception).printStackTrace()
        }
    }

    private fun handleError(error: ErrorEvent) {
        if (error.message == "ReferenceError: module is not defined") {
            return
        }

        log("Got error ${error.message} ${error.filename} ${error.lineno} ${error.colno} ${error.error}")

        pendingErrorEvents.add(error)
    }

    private fun createWorker(): Worker =
        Worker("worker.js").also { worker ->
            worker.onerror = { handleError(it as ErrorEvent) }
            worker.onmessage = ::handleMessage
        }
}
