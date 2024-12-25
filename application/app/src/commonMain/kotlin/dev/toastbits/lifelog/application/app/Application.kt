package dev.toastbits.lifelog.application.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.application.ComposeKitApplication
import dev.toastbits.composekit.components.platform.composable.onWindowBackPressed
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.composekit.util.composable.copy
import dev.toastbits.composekit.util.composable.plus
import dev.toastbits.composekit.util.platform.Platform
import dev.toastbits.composekit.util.thenIf
import dev.toastbits.lifelog.application.app.ui.PersistentBottomBar
import dev.toastbits.lifelog.application.app.ui.PersistentTopBar
import dev.toastbits.lifelog.application.core.FullContentScreen
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourcelist.DatabaseSourceListScreen
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourcelist.LogSaveScreenProviderImpl
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.DatabaseSourceLoadScreen
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.logview.screen.LogListScreen
import dev.toastbits.lifelog.application.settings.data.appsettings.AppSettingsImpl
import dev.toastbits.lifelog.application.settings.data.compositionlocal.LocalSettings
import dev.toastbits.lifelog.application.settings.domain.appsettings.AppSettings
import dev.toastbits.lifelog.application.settings.domain.model.SerialisedDatabaseSourceConfiguration
import dev.toastbits.lifelog.application.settings.domain.model.deserialiseConfiguration
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.application.worker.compositionlocal.LocalWorkerClient
import dev.toastbits.lifelog.extension.gdocs.GDocsExtension
import dev.toastbits.lifelog.extension.gdocs.MediaExtension
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtension

class Application(
    private val context: PlatformContext,
    private val workerClient: WorkerClient,
    preferences: PlatformSettings
): ComposeKitApplication(
    DatabaseSourceListScreen(),
    context,
    baseUiScale = BASE_UI_SCALE,
    baseFontScale = BASE_FONT_SCALE
) {
    override val settings: AppSettings = AppSettingsImpl(preferences)

    override val extraButtonsHandledExternally: Boolean
        get() = currentScreen !is FullContentScreen

    init {
        registerExtensions()
    }

    @Composable
    override fun Content(modifier: Modifier, contentPadding: PaddingValues) {
        LaunchedEffect(Unit) {
            openAutoOpenSource()
        }

        CompositionLocalProvider(
            LocalWorkerClient provides workerClient,
            LocalSettings provides settings
        ) {
            RootContent(modifier, contentPadding)
        }
    }

    fun onClose() {

    }

    private suspend fun openAutoOpenSource() {
        val autoOpenIndex: Int = settings.DatabaseSource.AUTO_OPEN_SOURCE_INDEX.get()
        if (autoOpenIndex < 0) {
            return
        }

        val serialisedSourceConfigurations: List<SerialisedDatabaseSourceConfiguration> = settings.DatabaseSource.DATABASE_SOURCES.get()
        val autoOpenConfiguration: DatabaseSourceConfiguration =
            serialisedSourceConfigurations.getOrNull(autoOpenIndex)?.let {
                settings.DatabaseSource.sourceTypeRegistry.deserialiseConfiguration(it)
            } ?: return

        val loadScreen: Screen =
            DatabaseSourceLoadScreen(
                autoOpenConfiguration,
                onLoaded = {
                    navigator.replaceScreen(LogListScreen(it, LogSaveScreenProviderImpl(autoOpenConfiguration)))
                },
                autoProceed = true
            )

        navigator.pushScreen(loadScreen)
    }

    @Composable
    private fun RootContent(modifier: Modifier, contentPadding: PaddingValues) {
        Box(modifier, contentAlignment = Alignment.Center) {
            val fullContentScreen: Boolean = navigator.currentScreen is FullContentScreen

            navigator.CurrentScreen(
                Modifier.fillMaxHeight().thenIf(!fullContentScreen) { widthIn(max = 1000.dp) },
                contentPadding + getPlatformPadding()
            ) { modifier, paddingValues, content ->
                Column(
                    modifier,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (!fullContentScreen) {
                        PersistentTopBar(Modifier.fillMaxWidth().padding(paddingValues.copy(bottom = 0.dp)))
                    }

                    content(
                        Modifier.fillMaxSize().weight(1f),
                        paddingValues.thenIf(!fullContentScreen) {
                            copy(top = 0.dp)
                        }
                    )

                    if (!fullContentScreen) {
                        PersistentBottomBar(Modifier.fillMaxWidth().padding(paddingValues.copy(top = 0.dp)))
                    }
                }
            }
        }
    }

    private fun registerExtensions() {
        settings.Database.extensionRegistry.registerExtension(MediaExtension())
        settings.Database.extensionRegistry.registerExtension(MediaWatchExtension())
        settings.Database.extensionRegistry.registerExtension(GDocsExtension())
    }

    private fun getPlatformPadding(): PaddingValues =
        when (Platform.current) {
            Platform.ANDROID -> PaddingValues(horizontal = 20.dp)
            Platform.DESKTOP,
            Platform.WEB -> PaddingValues(20.dp)
        }

    companion object {
        const val BASE_UI_SCALE: Float = 1.0f
        const val BASE_FONT_SCALE: Float = 1.2f
    }
}
