package dev.toastbits.lifelog.application.dbsource.inmemorygit.accessor

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.kogit.core.model.GitCredentials
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.model.LogDatabaseParseResult
import dev.toastbits.lifelog.application.dbsource.inmemorygit.configuration.InMemoryGitDatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper.toLoadProgress
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.application.worker.command.WorkerCommandInMemoryGitClone
import dev.toastbits.lifelog.application.worker.mapper.deserialise
import dev.toastbits.lifelog.application.worker.model.getOrThrow
import dev.toastbits.lifelog.core.accessor.helper.LogDatabaseParseHelper
import dev.toastbits.lifelog.core.specification.converter.ParseAlertData
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import kotlinx.coroutines.CoroutineDispatcher

class InMemoryGitDatabaseAccessor(
    private val workerClient: WorkerClient,
    private val configuration: InMemoryGitDatabaseSourceConfiguration,
    private val databaseConfigurationProvider: suspend () -> LogDatabaseConfiguration,
    private val gitCredentialsProvider: suspend () -> GitCredentials?,
    private val ioDispatcher: CoroutineDispatcher
): DatabaseAccessor {
    override suspend fun loadOnlineDatabase(onProgress: (LoadProgress) -> Unit): Result<LogDatabaseParseResult> = runCatching {
        val databaseConfiguration: LogDatabaseConfiguration = databaseConfigurationProvider()
        val gitCredentials: GitCredentials? = gitCredentialsProvider()

        val command: WorkerCommandInMemoryGitClone = WorkerCommandInMemoryGitClone(configuration.repositoryUrl, configuration.branchName, gitCredentials)

        val fileStructureResult: WorkerCommandInMemoryGitClone.Response =
            workerClient.executeCommand<WorkerCommandInMemoryGitClone.Response>(
                command,
                onProgress = { progress ->
                    val loadProgress: LoadProgress =
                        progress.toLoadProgress()
                        ?: return@executeCommand
                    onProgress(loadProgress)
                }
            ).getOrThrow().getOrThrow()

        val fileStructure: FileStructure = fileStructureResult.transferFileStructure.deserialise()

        val parser: LogDatabaseParseHelper = LogDatabaseParseHelper(databaseConfiguration, ioDispatcher)
        val alerts: MutableList<ParseAlertData> = mutableListOf()
        val database: LogDatabase = parser.parseFileStructure(fileStructure, alerts::add)

        return@runCatching LogDatabaseParseResult(database, alerts)
    }
}
