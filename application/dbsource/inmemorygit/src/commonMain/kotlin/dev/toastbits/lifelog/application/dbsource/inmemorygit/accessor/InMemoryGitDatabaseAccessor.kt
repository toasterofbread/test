package dev.toastbits.lifelog.application.dbsource.inmemorygit.accessor

import dev.toastbits.composekit.util.runSuspendCatching
import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.kogit.core.filestructure.FileStructureData
import dev.toastbits.kogit.core.model.GitCredentials
import dev.toastbits.kogit.core.provider.GitCredentialsProvider
import dev.toastbits.kogit.memory.model.GitRef
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseSaver
import dev.toastbits.lifelog.application.dbsource.domain.model.LogDatabaseParseResult
import dev.toastbits.lifelog.application.dbsource.inmemorygit.configuration.InMemoryGitDatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper.toLoadProgress
import dev.toastbits.lifelog.application.dbsource.inmemorygit.model.InMemoryGitLogDatabase
import dev.toastbits.lifelog.application.dbsource.inmemorygit.util.GitRepositoryFileUrlProvider
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.application.worker.command.WorkerCommandInMemoryGitClone
import dev.toastbits.lifelog.application.worker.command.normaliseRoot
import dev.toastbits.lifelog.application.worker.mapper.deserialise
import dev.toastbits.lifelog.application.worker.model.getOrThrow
import dev.toastbits.lifelog.core.accessor.helper.LogDatabaseParseHelper
import dev.toastbits.lifelog.core.specification.converter.ParseAlertData
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.database.LogDatabaseData
import kotlinx.coroutines.CoroutineDispatcher
import okio.Path
import okio.Path.Companion.toPath

class InMemoryGitDatabaseAccessor(
    private val workerClient: WorkerClient,
    private val configuration: InMemoryGitDatabaseSourceConfiguration,
    private val databaseConfigurationProvider: suspend () -> LogDatabaseConfiguration,
    private val gitCredentialsProvider: GitCredentialsProvider?,
    private val ioDispatcher: CoroutineDispatcher
): DatabaseAccessor<InMemoryGitLogDatabase> {
    override val saver: DatabaseSaver<InMemoryGitLogDatabase> =
        InMemoryGitDatabaseSaver(
            workerClient,
            configuration,
            databaseConfigurationProvider,
            ::getGitCredentials
        )

    override suspend fun loadOnlineDatabase(
        onProgress: (LoadProgress) -> Unit
    ): Result<LogDatabaseParseResult<InMemoryGitLogDatabase>> = runSuspendCatching {
        val databaseConfiguration: LogDatabaseConfiguration = databaseConfigurationProvider()
        val directoryPath: Path = configuration.directoryPath.toPath().normaliseRoot()

        val command: WorkerCommandInMemoryGitClone =
            WorkerCommandInMemoryGitClone(
                repositoryUrl = configuration.repositoryUrl,
                branch = GitRef.Branch(configuration.branchName),
                directoryPath = directoryPath.toString(),
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
        var rootNode: FileStructure.Node.Directory = fileStructure.root

        for (segment in directoryPath.segments) {
            val node: FileStructure.Node? = rootNode.nodes[segment]
            when (node) {
                is FileStructure.Node.Directory -> rootNode = node
                null -> throw RuntimeException("Subdirectory $directoryPath (${configuration.directoryPath}) not found in file structure")
                else -> throw RuntimeException("Path for subdirectory $directoryPath (${configuration.directoryPath}) points to a file")
            }
        }

        val parser: LogDatabaseParseHelper = LogDatabaseParseHelper(databaseConfiguration, ioDispatcher)
        val alerts: MutableList<ParseAlertData> = mutableListOf()
        val databaseData: LogDatabaseData = parser.parseFileStructure(FileStructureData(rootNode), alerts::add)

        val database: InMemoryGitLogDatabase =
            InMemoryGitLogDatabase.fromData(databaseData, gitCommitHash = fileStructureResult.headCommitHash)

        return@runSuspendCatching LogDatabaseParseResult(database, alerts)
    }

    override fun getFileLineUri(filePath: Path, lineIndex: UInt?): String? =
        GitRepositoryFileUrlProvider.getGitRepositoryFileUrl(configuration.repositoryUrl, configuration.branchName, filePath, lineIndex)

    private suspend fun getGitCredentials(): GitCredentials? =
        gitCredentialsProvider?.invoke(configuration.repositoryUrl)?.getOrThrow()
}
