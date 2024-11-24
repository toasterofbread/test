package dev.toastbits.lifelog.application.worker.mapper

import dev.toastbits.composekit.context.PlatformContext
import kotlinx.coroutines.Dispatchers

fun WorkerExecutionContext.Companion.default(context: PlatformContext): WorkerExecutionContext =
    WorkerExecutionContext(
        platformContext = context,
        ioDispatcher = Dispatchers.IO,
        defaultDispatcher = Dispatchers.Default
    )
