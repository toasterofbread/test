package dev.toastbits.lifelog.application.cache

import dev.toastbits.kogit.core.filestructure.FileStructure

expect class PlatformFileStructureCacher(id: String): FileStructureCacher {
    override suspend fun load(): Result<FileStructure?>
    override suspend fun save(database: FileStructure, onProgress: (Int, Int) -> Unit): Result<Unit>
    override suspend fun clear(): Result<Unit>
}
