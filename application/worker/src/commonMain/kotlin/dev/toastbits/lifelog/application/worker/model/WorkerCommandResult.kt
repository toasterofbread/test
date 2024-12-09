package dev.toastbits.lifelog.application.worker.model

import dev.toastbits.lifelog.application.worker.command.WorkerCommandProgress
import dev.toastbits.lifelog.application.worker.command.WorkerCommandResponse
import kotlinx.serialization.Serializable

@Serializable
sealed interface WorkerCommandResult {
    @Serializable
    data class Success(val response: WorkerCommandResponse): WorkerCommandResult
    @Serializable
    data class Progress(val progress: WorkerCommandProgress): WorkerCommandResult
    @Serializable
    data class Failure(val exception: WorkerException): WorkerCommandResult
}

fun Throwable.toResult(): WorkerCommandResult.Failure =
    WorkerCommandResult.Failure(toWorkerException())
