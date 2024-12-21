
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.singleWindowApplication
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.composekit.settings.PlatformSettingsImpl
import dev.toastbits.lifelog.application.app.Application
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.application.worker.mapper.WorkerExecutionContext
import dev.toastbits.lifelog.application.worker.mapper.default
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.awt.Dimension
import kotlin.time.Duration.Companion.seconds

fun main() = runBlocking {
    val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())

    val context: PlatformContext =
        PlatformContext("lifelog", coroutineScope)

    val preferences: PlatformSettings =
        PlatformSettingsImpl.getInstance(
            context.getFilesDir()!!.resolve("settings.json").file,
        )

    val workerExecutionContext: WorkerExecutionContext = WorkerExecutionContext.default(context)
    val workerClient: WorkerClient = WorkerClient(workerExecutionContext)

    val application: Application = Application(context, workerClient, preferences)
    singleWindowApplication(
        onKeyEvent = application::onKeyEvent,
        exitProcessOnExit = false
    ) {
        LaunchedEffect(Unit) {
            window.size = Dimension(1280, 900)
        }
        application.Main()
    }

    application.onClose()

    withTimeout(2.seconds) {
        coroutineScope.coroutineContext.job.cancelAndJoin()
    }
}
