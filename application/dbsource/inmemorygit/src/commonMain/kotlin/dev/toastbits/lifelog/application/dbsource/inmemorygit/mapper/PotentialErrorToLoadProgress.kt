package dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper

import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.worker.command.WorkerCommandProgress
import lifelog.application.dbsource.inmemorygit.generated.resources.Res
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_potential_error
import org.jetbrains.compose.resources.StringResource

internal fun WorkerCommandProgress.PotentialError.toLoadProgress(): LoadProgress =
    object : LoadProgress {
        override val type: LoadProgress.Type = LoadProgress.Type.Generic
        override val isError: Boolean = true

        override fun getMessageResource(): StringResource =
            Res.string.accessor_progress_potential_error

        override suspend fun getProgressMessage(): String = message
    }
