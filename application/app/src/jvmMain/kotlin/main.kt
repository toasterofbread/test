import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.singleWindowApplication
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.composekit.settings.PlatformSettingsJson
import dev.toastbits.lifelog.application.app.Application
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.application.worker.mapper.WorkerExecutionContext
import dev.toastbits.lifelog.application.worker.mapper.default
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import lifelog.application.app.generated.resources.Res
import lifelog.application.app.generated.resources.app_name
import org.jetbrains.compose.resources.getString
import java.awt.Dimension

fun main() = runBlocking {
    val coroutineScope: CoroutineScope = CoroutineScope(Job())
    val context: PlatformContext = PlatformContext(getString(Res.string.app_name), coroutineScope)
    val prefs: PlatformSettings = PlatformSettingsJson(context.getFilesDir()!!.resolve("settings.json"))

    val workerExecutionContext: WorkerExecutionContext = WorkerExecutionContext.default(context)
    val workerClient: WorkerClient = WorkerClient(workerExecutionContext)

    val application: Application = Application(context, workerClient, prefs)
    singleWindowApplication(
        onKeyEvent = application::onKeyEvent
    ) {
        LaunchedEffect(Unit) {
            window.size = Dimension(1280, 900)
        }
        application.Main()
    }

    application.onClose()
}
