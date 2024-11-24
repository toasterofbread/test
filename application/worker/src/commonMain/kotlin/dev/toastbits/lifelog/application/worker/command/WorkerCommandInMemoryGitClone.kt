package dev.toastbits.lifelog.application.worker.command

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.kogit.core.filestructure.countFiles
import dev.toastbits.kogit.core.model.GitCredentials
import dev.toastbits.kogit.memory.handler.stage.GitHandlerStage
import dev.toastbits.kogit.memory.helper.GitHelper
import dev.toastbits.lifelog.application.worker.cache.LocalGitObjectCache
import dev.toastbits.lifelog.application.worker.mapper.WorkerExecutionContext
import dev.toastbits.lifelog.application.worker.mapper.toTransferable
import dev.toastbits.lifelog.application.worker.model.TransferableFileStructure
import dev.toastbits.lifelog.application.worker.model.WorkerCommandResult
import dev.toastbits.lifelog.application.worker.model.toResult
import io.ktor.client.HttpClient
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class WorkerCommandInMemoryGitClone(
    val repositoryUrl: String,
    val branchName: String,
    val gitCredentials: GitCredentials?
): WorkerCommand {
    override suspend fun execute(
        context: WorkerExecutionContext,
        onProgress: (WorkerCommandProgress) -> Unit
    ): WorkerCommandResult {
        val cache: LocalGitObjectCache =
            LocalGitObjectCache.getInstance(repositoryUrl, context.platformContext)
                .fold(
                    onSuccess = { it },
                    onFailure = { return it.toResult() }
                )

        val httpClient: HttpClient = HttpClient()
        val gitHelper: GitHelper = GitHelper(httpClient, context.ioDispatcher, context.defaultDispatcher, gitCredentials)

        val fileStructure: FileStructure =
            gitHelper.cloneToFileStructure(repositoryUrl, branchName, cache) { stage, part, total ->
                onProgress(Progress(stage, part, total))
            }.fold(
                onSuccess = { it },
                onFailure = { return RuntimeException("Cloning $repositoryUrl:$branchName with $gitCredentials failed", it).toResult() }
            )

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
                return RuntimeException("Serialising file structure from $repositoryUrl:$branchName with $gitCredentials failed", e).toResult()
            }

        return WorkerCommandResult.Success(Response(transferableFileStructure))
    }

    @Serializable
    data class Progress(val stage: GitHandlerStage, val part: Long?, val total: Long?): WorkerCommandProgress

    @Serializable
    data class Response(val transferFileStructure: TransferableFileStructure): WorkerCommandResponse
}
