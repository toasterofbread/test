package dev.toastbits.lifelog.core.accessor.impl.git

import dev.toastbits.kogit.system.GitWrapper
import dev.toastbits.lifelog.core.accessor.DatabaseFilesGenerator
import dev.toastbits.lifelog.core.accessor.DatabaseFilesParser
import dev.toastbits.lifelog.core.accessor.LocalLogDatabaseAccessor
import dev.toastbits.lifelog.core.accessor.RemoteLogDatabaseAccessor
import dev.toastbits.lifelog.core.accessor.impl.getDatabaseFileStructure
import dev.toastbits.lifelog.core.accessor.model.GitRemoteBranch
import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.kogit.core.filestructure.walkFiles
import dev.toastbits.lifelog.core.specification.converter.GenerateAlertData
import dev.toastbits.lifelog.core.specification.converter.ParseAlertData
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import okio.FileSystem
import okio.Path
import okio.SYSTEM

class GitLogDatabaseAccessor(
    private val repository: GitWrapper,
    private val remote: GitRemoteBranch?,

    private val filesParser: DatabaseFilesParser,
    private val filesGenerator: DatabaseFilesGenerator,
    private val fileSystem: FileSystem = FileSystem.SYSTEM
): LocalLogDatabaseAccessor, RemoteLogDatabaseAccessor, GitWrapper by repository {
    override suspend fun saveDatabaseLocally(database: LogDatabase, onAlert: (GenerateAlertData) -> Unit) {
        val fileStructure: FileStructure = filesGenerator.generateDatabaseFileStructure(database, onAlert)

        fileStructure.walkFiles { file, path ->
            val relativePath: Path = repository.directory.resolve(path)
            fileSystem.createDirectories(relativePath.parent!!)
            fileSystem.write(relativePath) {
                when (file) {
                    is FileStructure.Node.File.FileLines -> {
                        for (line in file.readLines()) {
                            writeUtf8(line)
                            writeUtf8("\n")
                        }
                    }
                    is FileStructure.Node.File.FileBytes -> {
                        val (bytes: ByteArray, range: IntRange) = file.readBytes()
                        write(bytes, range.first, range.size)
                    }
                }
            }
        }
    }

    override suspend fun loadDatabaseLocally(onAlert: (ParseAlertData) -> Unit): LogDatabase {
        val fileStructure: FileStructure = fileSystem.getDatabaseFileStructure(repository.directory)
        return filesParser.parseDatabaseFileStructure(fileStructure, onAlert)
    }

    override fun canSaveDatabaseRemotely(database: LogDatabase): Boolean =
        remote != null

    private suspend fun syncRepository() {
        checkNotNull(remote)

        repository.init()
        repository.remoteAdd(remote.remoteName, remote.remoteUrl)

        repository.fetch(remote.remoteName)

        if (repository.doesBranchExist("${remote.remoteName}/${remote.branch}")) {
            if (repository.doesBranchExist(remote.branch)) {
                repository.checkout(remote.branch)
            }
            else {
                repository.checkout("${remote.remoteName}/${remote.branch}")
                repository.checkout(remote.branch, createNew = true)
            }

            repository.pull(remote.remoteName, remote.branch)
        }
        else {
            repository.checkoutOrphan(remote.branch)

            for (path in fileSystem.list(repository.directory)) {
                if (path.name == ".git") {
                    continue
                }
                fileSystem.deleteRecursively(path, mustExist = true)
            }
        }
    }

    override suspend fun saveDatabaseRemotely(
        database: LogDatabase,
        message: String,
        onAlert: (GenerateAlertData) -> Unit
    ) {
        checkNotNull(remote)
        require(message.isNotBlank())

        syncRepository()
        saveDatabaseLocally(database, onAlert)

        // We need to add before checking for changes, because some might just be line ending changes
        repository.add(".")

        val changedFiles: List<Path> = repository.getUncommittedFiles()
        if (changedFiles.isEmpty()) {
            return
        }

        repository.commit(message)
        repository.push(remote.remoteName, branch = remote.branch)
    }

    override fun canLoadDatabaseRemotely(): Boolean =
        remote != null

    override suspend fun loadDatabaseRemotely(onAlert: (ParseAlertData) -> Unit): LogDatabase {
        checkNotNull(remote)
        syncRepository()
        return loadDatabaseLocally(onAlert)
    }
}

val IntRange.size: Int
    get() = last - first + 1
