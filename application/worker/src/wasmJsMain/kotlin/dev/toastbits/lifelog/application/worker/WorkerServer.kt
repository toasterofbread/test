package dev.toastbits.lifelog.application.worker

import dev.toastbits.lifelog.application.worker.command.WorkerCommand
import dev.toastbits.lifelog.application.worker.command.WorkerCommandCancelCurrent
import dev.toastbits.lifelog.application.worker.mapper.WorkerExecutionContext
import dev.toastbits.lifelog.application.worker.model.WorkerCommandResult
import dev.toastbits.lifelog.application.worker.model.toResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.encodeToString
import org.w3c.dom.DedicatedWorkerGlobalScope
import org.w3c.dom.MessageEvent

private external val self: DedicatedWorkerGlobalScope

internal class WorkerServer(private val context: WorkerExecutionContext) {
    private val coroutineScope: CoroutineScope =
        CoroutineScope(SupervisorJob())

    private val mutex: Mutex = Mutex()
    private val workerEncoder: WorkerEncoder = WorkerEncoder()

    private fun msg(message: String): String = "Worker (server): $message"
    private fun log(message: String) = println(msg(message))

    fun handleMessage(message: MessageEvent) {
        val command: WorkerCommand =
            try {
                workerEncoder.decode(message)
            }
            catch (e: Throwable) {
                val exception: Throwable = RuntimeException(msg("Deserialising command from '${message.data}' failed"), e)
                exception.printStackTrace()
                exception.toResult().post()
                return
            }

        if (command is WorkerCommandCancelCurrent) {
            log("Received WorkerCommandCancelCurrent, cancelling job(s)")
            coroutineScope.coroutineContext.cancelChildren()
            return
        }

        coroutineScope.launch {
            if (!mutex.tryLock()) {
                throw IllegalStateException("Commands executed simultaneously? ($command)")
            }

            try {
                val result: WorkerCommandResult =
                    try {
                        command.execute(context) { progress ->
                            WorkerCommandResult.Progress(progress).post()
                        }
                    }
                    catch (e: Throwable) {
                        RuntimeException("Exception occurred while executing command $command", e).toResult()
                    }

                result.post()
            }
            finally {
                mutex.unlock()
            }
        }.invokeOnCompletion { exception ->
            if (exception !is CancellationException) {
                RuntimeException("Uncaught exception occurred while executing command $command", exception)
                    .toResult()
                    .post()

                if (exception is JsException) {
                    mutex.unlock()
                }
            }
        }
    }

    fun handleError(message: JsAny? /* Event|String */, filename: String, lineNo: Int, colNo: Int, error: JsAny?) {
        log("Got error $message $filename $lineNo $colNo $error")
    }

    private fun WorkerCommandResult.post() {
        val serialisedResult: String =
            try {
                workerJson.encodeToString(this)
            }
            catch (e: Throwable) {
                val message: String = "Serialising result '$this' failed: ${e.message}"
                log(message)
                workerJson.encodeToString(RuntimeException(message, e).toResult())
            }

        workerEncoder.post(self, serialisedResult)
    }
}
