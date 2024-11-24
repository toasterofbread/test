package dev.toastbits.lifelog.application.worker.cache

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.lifelog.application.worker.GitDatabase
import dev.toastbits.lifelog.core.git.memory.model.GitObject
import dev.toastbits.lifelog.core.git.memory.model.GitObjectRegistry
import dev.toastbits.lifelog.core.git.memory.model.MutableGitObjectRegistry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class LocalGitObjectCache private constructor(
    private val database: GitDatabase,
    private val repositoryIdentifier: String
): MutableGitObjectRegistry {
    private val base64: Base64 = Base64
    private val objects: MutableMap<String, GitObjectData> = mutableMapOf()
    private val mutex: Mutex = Mutex()

    private data class GitObjectData(val obj: GitObject, var modified: Boolean) {
        val hash: String get() = obj.hash
        val type: GitObject.Type get() = obj.type
        val bytes: ByteArray get() = obj.bytes
    }

    suspend fun countObjectsToCommit(): Int = mutex.withLock {
        objects.values.count { it.modified }
    }

    suspend fun commit() = mutex.withLock {
        database.transaction {
            for (obj in objects.values) {
                if (!obj.modified) {
                    continue
                }

                database.objectQueries.set(repositoryIdentifier, obj.hash, obj.type.ordinal.toLong(), base64.encode(obj.bytes))
                obj.modified = false
            }
        }
    }

    override suspend fun getAvailableObjects(type: GitObject.Type?): Iterable<GitObjectRegistry.GitObjectInfo> = mutex.withLock {
        (
            database.objectQueries
                .list(repositoryIdentifier, type?.ordinal?.toLong())
                .awaitAsList()
                .asSequence()
                .mapNotNull { obj ->
                    if (objects.containsKey(obj.hash)) {
                        return@mapNotNull null
                    }
                    return@mapNotNull GitObjectRegistry.GitObjectInfo(obj.hash, obj.type.toGitObjectType())
                }
            + objects.asSequence().map { GitObjectRegistry.GitObjectInfo(it.key, it.value.type) }
        ).asIterable()
    }

    override suspend fun readObjectOrNull(ref: String): GitObject? = mutex.withLock {
        objects[ref]?.obj
        ?: database.objectQueries.get(repositoryIdentifier, ref).awaitAsOneOrNull()?.let { (dataBase64, type) ->
            GitObject(base64.decode(dataBase64), type.toGitObjectType(), ref)
        }
    }

    override suspend fun writeObject(obj: GitObject) = mutex.withLock {
        objects[obj.hash] = GitObjectData(obj, true)
    }

    companion object {
        private var gitDatabase: GitDatabase? = null

        suspend fun getInstance(repositoryIdentifier: String, context: PlatformContext): Result<LocalGitObjectCache> =
            runCatching {
                val database: GitDatabase =
                    gitDatabase ?: GitDatabase.createInstance(context).also { gitDatabase = it }

                return@runCatching LocalGitObjectCache(database, repositoryIdentifier)
            }
    }
}

private fun Long.toGitObjectType(): GitObject.Type =
    GitObject.Type.entries[this.toInt()]
