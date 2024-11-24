package dev.toastbits.lifelog.application.dbsource.domain.type

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.toastbits.composekit.settings.ui.component.item.SettingsItem
import dev.toastbits.kogit.core.model.GitCredentials
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.configuration.castType
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher

interface DatabaseSourceType<C: DatabaseSourceConfiguration> {
    fun isAvailableOnPlatform(): Boolean

    fun createNewConfiguration(): C
    suspend fun onConfigurationDeleted(configuration: C) = Unit

    fun createAccessor(
        workerClient: WorkerClient,
        configuration: C,
        databaseConfigurationProvider: suspend () -> LogDatabaseConfiguration,
        gitCredentialsProvider: suspend () -> GitCredentials?,
        httpClient: HttpClient,
        ioDispatcher: CoroutineDispatcher,
        workDispatcher: CoroutineDispatcher
    ): DatabaseAccessor

    fun serialiseConfiguration(configuration: C): String
    fun deserialiseConfiguration(serialisedConfiguration: String): C

    @Composable
    fun getName(): String

    @Composable
    fun getDescription(): String

    @Composable
    fun getIcon(): ImageVector

    fun getLazyListConfigurationItems(configuration: C, onChange: (C) -> Unit): List<SettingsItem>
}

fun <C: DatabaseSourceConfiguration> C.getLazyListConfigurationItems(onChange: (C) -> Unit): List<SettingsItem> =
    castType().getLazyListConfigurationItems(this@getLazyListConfigurationItems, onChange)
