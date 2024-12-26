package dev.toastbits.lifelog.application.worker.command

import dev.toastbits.kogit.core.filestructure.SerialisableFileStructure
import dev.toastbits.kogit.core.model.GitCredentials
import dev.toastbits.kogit.memory.handler.GitCommitGenerator.UserInfo
import dev.toastbits.kogit.memory.handler.GitPusher
import dev.toastbits.kogit.memory.handler.stage.GitHandlerStage
import dev.toastbits.kogit.memory.helper.GitHelper
import dev.toastbits.kogit.memory.model.GitObject
import dev.toastbits.kogit.memory.model.GitRef
import dev.toastbits.kogit.memory.model.readObject
import dev.toastbits.lifelog.application.worker.cache.LocalGitObjectCache
import dev.toastbits.lifelog.application.worker.mapper.WorkerExecutionContext
import dev.toastbits.lifelog.application.worker.model.WorkerCommandResult
import dev.toastbits.lifelog.application.worker.model.toResult
import dev.toastbits.lifelog.application.worker.model.toWorkerException
import io.ktor.client.HttpClient
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class WorkerCommandInMemoryGitCommit(
    val headCommitRef: String,
    val message: String,
    val author: UserInfo,
    val committer: UserInfo,
    val repositoryUrl: String,
    val branch: GitRef.Branch,
    val gitCredentials: GitCredentials?,
    val fileStructure: SerialisableFileStructure
): WorkerCommand {
    override suspend fun execute(
        context: WorkerExecutionContext,
        onProgress: (WorkerCommandProgress) -> Unit
    ): WorkerCommandResult {
        val cache: LocalGitObjectCache? =
            LocalGitObjectCache.getInstance(repositoryUrl, context.platformContext)
                .getOrElse {
                    onProgress(WorkerCommandProgress.FailedToCreateLocalGitObjectCache(it.toWorkerException()))
                    return@getOrElse null
                }

        val gitHelper: GitHelper =
            GitHelper(
                repositoryUrl = repositoryUrl,
                readRef = branch,
                writeBranch = branch,
                objectRegistry = cache,
                httpClient = HttpClient(),
                ioDispatcher = context.ioDispatcher,
                workDispatcher = context.defaultDispatcher,
                credentials = gitCredentials
            )

        val progressListener: GitHelper.ProgressListener =
            GitHelper.ProgressListener { stage, part, total ->
                onProgress(
                    Progress(stage, part, total)
                )
            }

        val headCommit: GitObject = gitHelper.objectRegistry.readObject(headCommitRef)
        val pushResult: GitPusher.Result =
            gitHelper.commitAndPushFileStructure(
                headCommit,
                fileStructure,
                message,
                author,
                committer,
                progressListener
            ).getOrElse {
                return it.toResult()
            }

        if (cache != null) {
            val toCommit: Int = cache.countObjectsToCommit()
            if (toCommit > 0) {
                onProgress(
                    Progress(
                        GitHandlerStage.WritingObjectsToCache,
                        null,
                        toCommit.toLong()
                    )
                )
                withContext(context.ioDispatcher) {
                    cache.commit()
                }
            }
        }

        return WorkerCommandResult.Success(Response(pushResult))
    }

    @Serializable
    data class Progress(val stage: GitHandlerStage, val part: Long?, val total: Long?): WorkerCommandProgress

    @Serializable
    data class Response(val pushResponse: GitPusher.Result): WorkerCommandResponse
}
