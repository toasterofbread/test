package dev.toastbits.lifelog.application.worker.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkerException(val stackTraceString: String, val exceptionClass: String)

fun Throwable.toWorkerException(): WorkerException =
    WorkerException(this.stackTraceToString(), this::class.toString())
