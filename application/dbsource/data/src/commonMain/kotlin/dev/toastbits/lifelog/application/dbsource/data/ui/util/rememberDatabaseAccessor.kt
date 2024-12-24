package dev.toastbits.lifelog.application.dbsource.data.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.configuration.castType
import dev.toastbits.lifelog.application.settings.data.compositionlocal.LocalSettings
import dev.toastbits.lifelog.application.settings.data.group.createGitCredentialsProvider
import dev.toastbits.lifelog.application.settings.domain.appsettings.AppSettings
import dev.toastbits.lifelog.application.settings.domain.group.getLogDatabaseConfiguration
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.application.worker.compositionlocal.LocalWorkerClient
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers

@Composable
fun rememberDatabaseAccessor(sourceConfiguration: DatabaseSourceConfiguration): DatabaseAccessor {
    val provider: () -> DatabaseAccessor = rememberDatabaseAccessorProvider(sourceConfiguration)
    return remember(provider) { provider() }
}

@Composable
fun rememberDatabaseAccessorProvider(sourceConfiguration: DatabaseSourceConfiguration): () -> DatabaseAccessor {
    val workerClient: WorkerClient = LocalWorkerClient.current
    val settings: AppSettings = LocalSettings.current

    return remember(sourceConfiguration) {{
        sourceConfiguration.castType().createAccessor(
            workerClient = workerClient,
            configuration = sourceConfiguration,
            databaseConfigurationProvider = settings.Database::getLogDatabaseConfiguration,
            gitCredentialsProvider = settings.DatabaseSource.createGitCredentialsProvider(),
            httpClient = HttpClient(),
            ioDispatcher = Dispatchers.Default,
            workDispatcher = Dispatchers.Default
        )
    }}
}
