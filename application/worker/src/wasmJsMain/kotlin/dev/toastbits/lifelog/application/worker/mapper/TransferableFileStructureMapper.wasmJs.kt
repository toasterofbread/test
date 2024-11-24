package dev.toastbits.lifelog.application.worker.mapper

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.kogit.core.filestructure.toSerialisable
import dev.toastbits.lifelog.application.worker.model.TransferableFileStructure

actual suspend fun FileStructure.toTransferable(onProgress: (Int) -> Unit): TransferableFileStructure =
   toSerialisable(onProgress)

actual fun TransferableFileStructure.deserialise(): FileStructure = this
