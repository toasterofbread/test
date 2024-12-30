package dev.toastbits.lifelog.application.dbsource.inmemorygit.accessor

import dev.toastbits.composekit.util.runSuspendCatching
import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.kogit.core.filestructure.MutableFileStructure.MutableNode.Directory
import dev.toastbits.kogit.core.filestructure.toSerialisable
import dev.toastbits.kogit.core.model.GitCredentials
import dev.toastbits.kogit.memory.handler.GitCommitGenerator.UserInfo
import dev.toastbits.kogit.memory.handler.GitPusher
import dev.toastbits.kogit.memory.model.GitRef
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseSaver
import dev.toastbits.lifelog.application.dbsource.domain.accessor.create
import dev.toastbits.lifelog.application.dbsource.inmemorygit.configuration.InMemoryGitDatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_serialising_file_structure
import dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper.toLoadProgress
import dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper.toSaveResult
import dev.toastbits.lifelog.application.dbsource.inmemorygit.model.InMemoryGitLogDatabase
import dev.toastbits.lifelog.application.worker.WorkerClient
import dev.toastbits.lifelog.application.worker.command.WorkerCommandInMemoryGitCommit
import dev.toastbits.lifelog.application.worker.model.getOrThrow
import dev.toastbits.lifelog.core.accessor.helper.LogDatabaseGenerateHelper
import dev.toastbits.lifelog.core.specification.converter.GenerateAlertData
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import okio.Path
import okio.Path.Companion.toPath

class InMemoryGitDatabaseSaver(
    private val workerClient: WorkerClient,
    private val configuration: InMemoryGitDatabaseSourceConfiguration,
    private val databaseConfigurationProvider: suspend () -> LogDatabaseConfiguration,
    private val getGitCredentials: suspend () -> GitCredentials?
): DatabaseSaver<InMemoryGitLogDatabase> {
    override suspend fun saveOnlineDatabase(
        database: InMemoryGitLogDatabase,
        message: String,
        author: UserInfo,
        committer: UserInfo,
        onProgress: (LoadProgress) -> Unit
    ): Result<DatabaseSaver.SaveResult<InMemoryGitLogDatabase>> = runSuspendCatching {
        val databaseConfiguration: LogDatabaseConfiguration = databaseConfigurationProvider()
        val directoryPath: Path = configuration.directoryPath.toPath(true)

        val alerts: MutableList<GenerateAlertData> = mutableListOf()
        val generator: LogDatabaseGenerateHelper = LogDatabaseGenerateHelper(databaseConfiguration)
        val fileStructure: FileStructure = generator.generateFileStructure(database, alerts::add)

        val rootNode: Directory = Directory()
        var currentNode: Directory = rootNode

        for (segment in directoryPath.segments) {
            val node: Directory = Directory()
            currentNode.nodes[segment] = node
            currentNode = node
        }

        val command: WorkerCommandInMemoryGitCommit =
            WorkerCommandInMemoryGitCommit(
                headCommitRef = database.gitCommitHash,
                message = message,
                author = author,
                committer = committer,
                repositoryUrl = configuration.repositoryUrl,
                branch = GitRef.Branch(configuration.branchName),
                directoryPath = configuration.directoryPath,
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
}
