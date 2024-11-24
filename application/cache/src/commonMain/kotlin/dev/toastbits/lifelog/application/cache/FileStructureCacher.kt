package dev.toastbits.lifelog.application.cache

import dev.toastbits.kogit.core.filestructure.FileStructure

interface FileStructureCacher {
    suspend fun load(): Result<FileStructure?>
    suspend fun save(database: FileStructure, onProgress: (Int, Int) -> Unit): Result<Unit>
    suspend fun clear(): Result<Unit>
}
