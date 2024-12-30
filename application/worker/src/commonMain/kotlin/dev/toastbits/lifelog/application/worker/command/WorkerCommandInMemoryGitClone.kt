package dev.toastbits.lifelog.application.worker.command

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.kogit.core.filestructure.countFiles
import dev.toastbits.kogit.core.model.GitCredentials
import dev.toastbits.kogit.memory.handler.stage.GitHandlerStage
import dev.toastbits.kogit.memory.helper.GitHelper
import dev.toastbits.kogit.memory.model.GitObject
import dev.toastbits.kogit.memory.model.GitRef
import dev.toastbits.lifelog.application.worker.cache.LocalGitObjectCache
import dev.toastbits.lifelog.application.worker.mapper.WorkerExecutionContext
import dev.toastbits.lifelog.application.worker.mapper.toTransferable
import dev.toastbits.lifelog.application.worker.model.TransferableFileStructure
import dev.toastbits.lifelog.application.worker.model.WorkerCommandResult
import dev.toastbits.lifelog.application.worker.model.toResult
import io.ktor.client.HttpClient
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okio.Path
import okio.Path.Companion.toPath

fun Path.normaliseRoot(): Path =
    "/".toPath().resolve(this, normalize = true)

@Serializable
data class WorkerCommandInMemoryGitClone(
    val repositoryUrl: String,
    val branch: GitRef.Branch,
    val directoryPath: String,
    val gitCredentials: GitCredentials?
): WorkerCommand {
    override suspend fun execute(
        context: WorkerExecutionContext,
        onProgress: (WorkerCommandProgress) -> Unit
    ): WorkerCommandResult {
        val cache: LocalGitObjectCache =
            LocalGitObjectCache.getInstance(repositoryUrl, context.platformContext)
                .getOrElse {
                    return it.toResult()
                }

        val httpClient: HttpClient = HttpClient()
        val gitHelper: GitHelper = GitHelper(
            repositoryUrl = repositoryUrl,
            readRef = branch,
            writeBranch = branch,
            objectRegistry = cache,
            httpClient = httpClient,
            ioDispatcher = context.ioDispatcher,
            workDispatcher = context.defaultDispatcher,
            credentials = gitCredentials
        )

        val directoryPathPath: Path = directoryPath.toPath().normaliseRoot()

        val (headCommit: GitObject, fileStructure: FileStructure) =
            gitHelper.cloneToFileStructure(
                shouldProcessTree = { path ->
                    directoryPathPath.isRoot || directoryPathPath.startsWith(path).also { println("$directoryPathPath startsWith $path = $it") }
                }
            ) { stage, part, total ->
                onProgress(Progress(stage, part, total))
            }.fold(
                onSuccess = { it },
                onFailure = { return RuntimeException("Cloning $repositoryUrl:$branch with $gitCredentials failed", it).toResult() }
            )

        if (fileStructure.nodes.isEmpty() && directoryPathPath != "/".toPath()) {
            return RuntimeException("Repository has no directory matching $directoryPathPath ($directoryPath)").toResult()
        }

        val toCommit: Int = cache.countObjectsToCommit()
        if (toCommit > 0) {
            onProgress(Progress(GitHandlerStage.WritingObjectsToCache, null, toCommit.toLong()))
            withContext(context.ioDispatcher) {
                cache.commit()
            }
        }

        val totalFiles: Long = fileStructure.countFiles().toLong()
        val transferableFileStructure: TransferableFileStructure =
            try {
                fileStructure.toTransferable { done ->
                    onProgress(Progress(GitHandlerStage.SerialisingFileStructure, done.toLong(), totalFiles))
                }
            }
            catch (e: Throwable) {
                return RuntimeException("Serialising file structure from $repositoryUrl:$branch with $gitCredentials failed", e).toResult()
            }

        return WorkerCommandResult.Success(Response(headCommit.hash, transferableFileStructure))
    }

    private fun Path.startsWith(other: Path): Boolean {
        var path: Path = this.normaliseRoot()
        val otherNormalised: Path = other.normaliseRoot()
        while (true) {
            if (path == otherNormalised) {
                return true
            }
            path = path.parent ?: return false
        }
    }

    @Serializable
    data class Progress(val stage: GitHandlerStage, val part: Long?, val total: Long?): WorkerCommandProgress

    @Serializable
    data class Response(val headCommitHash: String, val transferFileStructure: TransferableFileStructure): WorkerCommandResponse
}
