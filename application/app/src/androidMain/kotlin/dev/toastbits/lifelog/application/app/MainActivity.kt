package dev.toastbits.lifelog.application.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.core.view.WindowCompat
import dev.toastbits.composekit.platform.ApplicationContext
import dev.toastbits.composekit.platform.PlatformContext
import dev.toastbits.composekit.platform.PlatformContextImpl
import dev.toastbits.composekit.platform.preferences.PlatformPreferences
import dev.toastbits.composekit.platform.preferences.PlatformPreferencesImpl
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.application.worker.mapper.WorkerExecutionContext
import dev.toastbits.lifelog.application.worker.mapper.default
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    private var application: Application? = null
    private val coroutineScope: CoroutineScope = CoroutineScope(Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context: PlatformContext =
            PlatformContextImpl(
                this,
                coroutineScope,
                ApplicationContext(this)
            )

        val workerClient: WorkerClient =
            WorkerClient(
                WorkerExecutionContext.default(context)
            )

        val prefs: PlatformPreferences = PlatformPreferencesImpl.getInstance(this, Json)

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
