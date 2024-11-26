package dev.toastbits.lifelog.application.app

import androidx.compose.ui.window.CanvasBasedWindow
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.composekit.settings.cookies.BrowserCookies
import dev.toastbits.composekit.settings.cookies.CookiesPlatformSettings
import dev.toastbits.lifelog.application.worker.WorkerClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

fun main() {
    val coroutineScope: CoroutineScope = CoroutineScope(Job())

    val context: PlatformContext = PlatformContext(coroutineScope)
    val workerClient: WorkerClient = WorkerClient()
    val prefs: PlatformSettings = CookiesPlatformSettings(BrowserCookies())

    val application: Application = Application(context, workerClient, prefs)

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        application.Main()
    }

    application.onClose()
}
