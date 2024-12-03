package dev.toastbits.lifelog.application.dbsource.inmemorygit.accessor

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.kogit.core.filestructure.toSerialisable
import dev.toastbits.kogit.core.model.GitCredentials
import dev.toastbits.kogit.memory.handler.GitCommitGenerator.UserInfo
import dev.toastbits.kogit.memory.model.GitRef
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.accessor.create
import dev.toastbits.lifelog.application.dbsource.domain.model.LogDatabaseParseResult
import dev.toastbits.lifelog.application.dbsource.inmemorygit.configuration.InMemoryGitDatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper.toLoadProgress
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
import lifelog.application.dbsource.inmemorygit.generated.resources.Res
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_serialising_file_structure
import okio.Path

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

        val command: WorkerCommandInMemoryGitClone =
            WorkerCommandInMemoryGitClone(
                repositoryUrl = configuration.repositoryUrl,
                branch = GitRef.Branch(configuration.branchName),
                gitCredentials = gitCredentials
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

        return@runCatching LogDatabaseParseResult(database, alerts)
    }

    override suspend fun saveOnlineDatabase(
        database: LogDatabase,
        message: String,
        author: UserInfo,
        committer: UserInfo,
        onProgress: (LoadProgress) -> Unit
    ): Result<Unit> = runCatching {
        val databaseConfiguration: LogDatabaseConfiguration = databaseConfigurationProvider()
        val gitCredentials: GitCredentials? = gitCredentialsProvider()

        val alerts: MutableList<GenerateAlertData> = mutableListOf()
        val generator: LogDatabaseGenerateHelper = LogDatabaseGenerateHelper(databaseConfiguration)
        val fileStructure: FileStructure = generator.generateFileStructure(database, alerts::add)

        val command: WorkerCommandInMemoryGitCommit =
            WorkerCommandInMemoryGitCommit(
                headCommitRef = database.gitCommitRef!!,
                message = message,
                author = author,
                committer = committer,
                repositoryUrl = configuration.repositoryUrl,
                branch = GitRef.Branch(configuration.branchName),
                gitCredentials = gitCredentials,
                fileStructure = fileStructure.toSerialisable {
                    onProgress(LoadProgress.Type.GENERIC.create(it.toLong(), null, Res.string.accessor_progress_serialising_file_structure))
                }
            )

        workerClient.executeCommand<WorkerCommandInMemoryGitCommit.Response>(
            command,
            onProgress = { progress ->
                val loadProgress: LoadProgress =
                    progress.toLoadProgress()
                        ?: return@executeCommand
                onProgress(loadProgress)
            }
        ).getOrThrow().getOrThrow()

        return@runCatching
    }

    override fun getFileLineUri(filePath: Path, lineIndex: UInt?): String? =
        GitRepositoryFileUrlProvider.getGitRepositoryFileUrl(configuration.repositoryUrl, configuration.branchName, filePath, lineIndex)
}
