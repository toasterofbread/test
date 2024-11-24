import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.lifelog.application.worker.WorkerServer
import dev.toastbits.lifelog.application.worker.mapper.WorkerExecutionContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.w3c.dom.DedicatedWorkerGlobalScope

private external val self: DedicatedWorkerGlobalScope

fun main() {
    val coroutineScope: CoroutineScope = CoroutineScope(Job())
    val context: PlatformContext = PlatformContext(coroutineScope)

    val workerExecutionContext: WorkerExecutionContext =
        WorkerExecutionContext(
            platformContext = context,
            ioDispatcher = Dispatchers.Default,
            defaultDispatcher = Dispatchers.Default
        )

    val server: WorkerServer = WorkerServer(workerExecutionContext)
    println("Worker started")

    // First message required for activation of worker?
    // See WorkerClient.firstMessageSkipped
    self.postMessage("Worker started".toJsString())

    self.onmessage = server::handleMessage
    self.onerror = { a, b, c, d, e -> server.handleError(a, b, c, d, e); null }
}
