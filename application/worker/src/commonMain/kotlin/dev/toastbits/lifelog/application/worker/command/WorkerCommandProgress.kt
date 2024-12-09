package dev.toastbits.lifelog.application.worker.command

import dev.toastbits.lifelog.application.worker.model.WorkerException
import kotlinx.serialization.Serializable

@Serializable
sealed interface WorkerCommandProgress {
    @Serializable
    data class PotentialError(val message: String): WorkerCommandProgress

    @Serializable
    data class FailedToCreateLocalGitObjectCache(val exception: WorkerException): WorkerCommandProgress

    @Serializable
    data object WaitingForWorker: WorkerCommandProgress

    @Serializable
    data object WorkerStarted: WorkerCommandProgress
}
