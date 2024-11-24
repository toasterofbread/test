package dev.toastbits.lifelog.core.accessor.impl

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.kogit.core.filestructure.MutableFileStructure
import okio.FileSystem
import okio.Path

internal fun FileSystem.getDatabaseFileStructure(path: Path): FileStructure {
    val fileStructure: MutableFileStructure = MutableFileStructure()

    for (file in listRecursively(path)) {
        if (metadataOrNull(file)?.isRegularFile != true) {
            continue
        }

        val relativeFile: Path = file.relativeTo(path)
        if (relativeFile.segments.firstOrNull() == ".git") {
            continue
        }

        fileStructure.createFile(relativeFile, OkioNodeFile(file, this))
    }

    return fileStructure
}

private data class OkioNodeFile(private val path: Path, private val fileSystem: FileSystem): FileStructure.Node.File.FileLines {
    override suspend fun readLines(): Sequence<String> = sequence {
        fileSystem.read(path) {
            while (true) {
                val line: String = readUtf8Line() ?: break
                yield(line)
            }
        }
    }
}
