package dev.toastbits.lifelog.application.worker.mapper

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.lifelog.application.worker.model.TransferableFileStructure

actual suspend fun FileStructure.toTransferable(onProgress: (Int) -> Unit): TransferableFileStructure =
    TransferableFileStructure(this)

actual fun TransferableFileStructure.deserialise(): FileStructure = this
