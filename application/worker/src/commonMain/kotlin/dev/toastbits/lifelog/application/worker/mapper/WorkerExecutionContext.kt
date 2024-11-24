package dev.toastbits.lifelog.application.worker.mapper

import dev.toastbits.composekit.context.PlatformContext
import kotlinx.coroutines.CoroutineDispatcher

data class WorkerExecutionContext(
    val platformContext: PlatformContext,
    val ioDispatcher: CoroutineDispatcher,
    val defaultDispatcher: CoroutineDispatcher
) {
    companion object
}
