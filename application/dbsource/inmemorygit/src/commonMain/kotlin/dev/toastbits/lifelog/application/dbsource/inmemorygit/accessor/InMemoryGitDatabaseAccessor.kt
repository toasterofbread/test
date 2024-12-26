package dev.toastbits.lifelog.application.dbsource.inmemorygit.accessor

import dev.toastbits.composekit.util.runSuspendCatching
import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.kogit.core.filestructure.toSerialisable
import dev.toastbits.kogit.core.model.GitCredentials
import dev.toastbits.kogit.core.provider.GitCredentialsProvider
import dev.toastbits.kogit.memory.handler.GitCommitGenerator.UserInfo
import dev.toastbits.kogit.memory.handler.GitPusher
import dev.toastbits.kogit.memory.model.GitRef
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.SaveResult
import dev.toastbits.lifelog.application.dbsource.domain.accessor.create
import dev.toastbits.lifelog.application.dbsource.domain.model.LogDatabaseParseResult
import dev.toastbits.lifelog.application.dbsource.inmemorygit.configuration.InMemoryGitDatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_serialising_file_structure
import dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper.toLoadProgress
import dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper.toSaveResult
import dev.toastbits.lifelog.application.dbsource.inmemorygit.util.GitRepositoryFileUrlProvider
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.application.worker.command.WorkerCommandInMemoryGitClone
import dev.toastbits.lifelog.application.worker.command.WorkerCommandInMemoryGitCommit
import dev.toastbits.lifelog.application.worker.mapper.deserialise
import dev.toastbits.lifelog.application.worker.model.getOrThrow
import dev.toastbits.lifelog.core.accessor.helper.LogDatabaseGenerateHelper
import dev.toastbits.lifelog.core.accessor.helper.LogDatabaseParseHelper
import dev.toastbits.lifelog.core.specification.converter.GenerateAlertData
import dev.toastbits.lifelog.core.specification.converter.ParseAlertData
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import okio.Path

class InMemoryGitDatabaseAccessor(
    private val workerClient: WorkerClient,
    private val configuration: InMemoryGitDatabaseSourceConfiguration,
    private val databaseConfigurationProvider: suspend () -> LogDatabaseConfiguration,
    private val gitCredentialsProvider: GitCredentialsProvider?,
    private val ioDispatcher: CoroutineDispatcher
): DatabaseAccessor {
    override suspend fun loadOnlineDatabase(onProgress: (LoadProgress) -> Unit): Result<LogDatabaseParseResult> = runSuspendCatching {
        val databaseConfiguration: LogDatabaseConfiguration = databaseConfigurationProvider()

        val command: WorkerCommandInMemoryGitClone =
            WorkerCommandInMemoryGitClone(
                repositoryUrl = configuration.repositoryUrl,
                branch = GitRef.Branch(configuration.branchName),
                gitCredentials = getGitCredentials()
            )

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
        val database: LogDatabase = parser.parseFileStructure(fileStructure, fileStructureResult.headCommitRef, alerts::add)

        return@runSuspendCatching LogDatabaseParseResult(database, alerts)
    }

    override suspend fun saveOnlineDatabase(
        database: LogDatabase,
        message: String,
        author: UserInfo,
        committer: UserInfo,
        onProgress: (LoadProgress) -> Unit
    ): Result<SaveResult> = runSuspendCatching {
        val databaseConfiguration: LogDatabaseConfiguration = databaseConfigurationProvider()

        val alerts: MutableList<GenerateAlertData> = mutableListOf()
        val generator: LogDatabaseGenerateHelper = LogDatabaseGenerateHelper(databaseConfiguration)
        val fileStructure: FileStructure = generator.generateFileStructure(database, alerts::add)

        val command: WorkerCommandInMemoryGitCommit =
            WorkerCommandInMemoryGitCommit(
                headCommitRef = database.gitCommitHash!!,
                message = message,
                author = author,
                committer = committer,
                repositoryUrl = configuration.repositoryUrl,
                branch = GitRef.Branch(configuration.branchName),
                gitCredentials = getGitCredentials(),
                fileStructure = fileStructure.toSerialisable {
                    onProgress(LoadProgress.Type.Generic.create(it.toLong(), null, Res.string.accessor_progress_serialising_file_structure))
                }
            )

        val pushResult: GitPusher.Result =
            workerClient.executeCommand<WorkerCommandInMemoryGitCommit.Response>(
                command,
                onProgress = { progress ->
                    val loadProgress: LoadProgress =
                        progress.toLoadProgress()
                            ?: return@executeCommand
                    onProgress(loadProgress)
                }
            ).getOrThrow().getOrThrow().pushResponse

        return@runSuspendCatching pushResult.toSaveResult(database)
    }

    override fun getFileLineUri(filePath: Path, lineIndex: UInt?): String? =
        GitRepositoryFileUrlProvider.getGitRepositoryFileUrl(configuration.repositoryUrl, configuration.branchName, filePath, lineIndex)

    private suspend fun getGitCredentials(): GitCredentials? =
        gitCredentialsProvider?.invoke(configuration.repositoryUrl)?.getOrThrow()
}
