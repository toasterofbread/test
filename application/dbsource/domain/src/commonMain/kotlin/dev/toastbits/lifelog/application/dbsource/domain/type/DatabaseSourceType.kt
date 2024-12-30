package dev.toastbits.lifelog.application.dbsource.domain.type

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.ImageVector
import dev.toastbits.composekit.settingsitem.domain.SettingsItem
import dev.toastbits.kogit.core.provider.GitCredentialsProvider
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher

interface DatabaseSourceType<C: DatabaseSourceConfiguration<T>, T: LogDatabase> {
    fun isAvailableOnPlatform(): Boolean

    fun createNewConfiguration(): C
    suspend fun onConfigurationDeleted(configuration: C) = Unit

    fun createAccessor(
        workerClient: WorkerClient,
        configuration: C,
        databaseConfigurationProvider: suspend () -> LogDatabaseConfiguration,
        gitCredentialsProvider: GitCredentialsProvider?,
        httpClient: HttpClient,
        ioDispatcher: CoroutineDispatcher,
        workDispatcher: CoroutineDispatcher
    ): DatabaseAccessor<T>

    fun serialiseConfiguration(configuration: C): String
    fun deserialiseConfiguration(serialisedConfiguration: String): C

    @Composable
    fun getName(): String

    @Composable
    fun getDescription(): String

    @Composable
    fun getIcon(): ImageVector

    fun getLazyListConfigurationItems(configurationState: MutableState<C>): List<SettingsItem>
}
