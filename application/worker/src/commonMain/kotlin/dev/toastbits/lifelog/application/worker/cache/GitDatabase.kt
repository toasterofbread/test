package dev.toastbits.lifelog.application.worker.cache

import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.lifelog.application.worker.GitDatabase

internal expect suspend fun GitDatabase.Companion.createInstance(context: PlatformContext): GitDatabase
