package dev.toastbits.lifelog.application.worker.mapper

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.lifelog.application.worker.model.TransferableFileStructure

expect suspend fun FileStructure.toTransferable(onProgress: (Int) -> Unit = {}): TransferableFileStructure

expect fun TransferableFileStructure.deserialise(): FileStructure
