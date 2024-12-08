package dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper

import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.accessor.message
import dev.toastbits.lifelog.application.worker.command.WorkerCommandClearGitRepositoryCache
import dev.toastbits.lifelog.application.worker.command.WorkerCommandInMemoryGitClone
import dev.toastbits.lifelog.application.worker.command.WorkerCommandInMemoryGitCommit
import dev.toastbits.lifelog.application.worker.command.WorkerCommandProgress
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_waiting_for_worker
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_worker_started

internal fun WorkerCommandProgress.toLoadProgress(): LoadProgress? =
    when (this) {
        is WorkerCommandProgress.PotentialError -> this.toLoadProgress()
        is WorkerCommandInMemoryGitClone.Progress -> this.stage.toLoadProgress(part, total)
        is WorkerCommandInMemoryGitCommit.Progress -> this.stage.toLoadProgress(part, total)
        WorkerCommandProgress.WaitingForWorker -> LoadProgress.message(Res.string.accessor_progress_waiting_for_worker)
        WorkerCommandProgress.WorkerStarted -> LoadProgress.message(Res.string.accessor_progress_worker_started)

        is WorkerCommandClearGitRepositoryCache.Progress -> null
    }
