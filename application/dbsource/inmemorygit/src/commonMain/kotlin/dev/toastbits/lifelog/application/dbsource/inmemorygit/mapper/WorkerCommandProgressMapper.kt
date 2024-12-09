package dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper

import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.accessor.error
import dev.toastbits.lifelog.application.dbsource.domain.accessor.message
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_failed_to_create_local_git_object_cache
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_potential_error
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_waiting_for_worker
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_worker_started
import dev.toastbits.lifelog.application.worker.command.WorkerCommandClearGitRepositoryCache
import dev.toastbits.lifelog.application.worker.command.WorkerCommandInMemoryGitClone
import dev.toastbits.lifelog.application.worker.command.WorkerCommandInMemoryGitCommit
import dev.toastbits.lifelog.application.worker.command.WorkerCommandProgress
import org.jetbrains.compose.resources.getString

internal fun WorkerCommandProgress.toLoadProgress(): LoadProgress? =
    when (this) {
        is WorkerCommandProgress.PotentialError ->
            LoadProgress.error(Res.string.accessor_progress_potential_error, { message })
        is WorkerCommandProgress.FailedToCreateLocalGitObjectCache ->
            LoadProgress.error(Res.string.accessor_progress_failed_to_create_local_git_object_cache, { "${getString(it)} | $exception" })
        is WorkerCommandInMemoryGitClone.Progress ->
            this.stage.toLoadProgress(part, total)
        is WorkerCommandInMemoryGitCommit.Progress ->
            this.stage.toLoadProgress(part, total)
        WorkerCommandProgress.WaitingForWorker ->
            LoadProgress.message(Res.string.accessor_progress_waiting_for_worker)
        WorkerCommandProgress.WorkerStarted ->
            LoadProgress.message(Res.string.accessor_progress_worker_started)

        is WorkerCommandClearGitRepositoryCache.Progress ->
            null
    }
