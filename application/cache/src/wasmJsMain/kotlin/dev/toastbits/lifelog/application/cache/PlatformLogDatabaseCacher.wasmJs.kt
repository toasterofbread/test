package dev.toastbits.lifelog.application.cache

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.lifelog.core.filestructure.SerialisableFileStructure
import dev.toastbits.lifelog.core.filestructure.countFiles
import dev.toastbits.lifelog.core.filestructure.toSerialisable
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class PlatformFileStructureCacher actual constructor(private val id: String): FileStructureCacher {
    private val json: Json = Json

    actual override suspend fun load(): Result<FileStructure?> = runCatching {
        val serialisedFileStructure: String = localStorage.getItem(id) ?: return@runCatching null
        val fileStructure: SerialisableFileStructure = json.decodeFromString(serialisedFileStructure)
        return@runCatching fileStructure
    }

    actual override suspend fun save(database: FileStructure, onProgress: (Int, Int) -> Unit): Result<Unit> = runCatching {
        val totalFiles: Int = database.countFiles()
        val serialised: SerialisableFileStructure = database.toSerialisable { onProgress(it, totalFiles) }
        localStorage.setItem(id, json.encodeToString(serialised))
    }

    actual override suspend fun clear(): Result<Unit> = runCatching {
        localStorage.removeItem(id)
    }
}
