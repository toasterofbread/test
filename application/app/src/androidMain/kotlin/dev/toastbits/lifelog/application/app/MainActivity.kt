package dev.toastbits.lifelog.application.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.toastbits.composekit.context.ApplicationContext
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.composekit.settings.PlatformSettingsImpl
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.application.worker.mapper.WorkerExecutionContext
import dev.toastbits.lifelog.application.worker.mapper.default
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class MainActivity : ComponentActivity() {
    private var application: Application? = null
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context: PlatformContext =
            PlatformContext(
                this,
                coroutineScope,
                ApplicationContext(this)
            )

        val workerClient: WorkerClient =
            WorkerClient(
                WorkerExecutionContext.default(context)
            )

        val prefs: PlatformSettings = PlatformSettingsImpl.getInstance(this)

        val currentApplication: Application =
            Application(
                context,
                workerClient,
                prefs
            )

        application = currentApplication

        enableEdgeToEdge()

        setContent {
            currentApplication.Main()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        application?.also {
            it.onClose()
            application = null
        }
    }
}
