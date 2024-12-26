package dev.toastbits.lifelog.application.worker.cache

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.kogit.memory.model.GitObject
import dev.toastbits.kogit.memory.model.GitObjectInfo
import dev.toastbits.kogit.memory.model.MutableGitObjectRegistry
import dev.toastbits.lifelog.application.worker.GitDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.collections.List
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

    override suspend fun hasObject(hash: String): Boolean =
        objects.contains(hash)
            || database.objectQueries.getType(repositoryIdentifier, hash).awaitAsOneOrNull() != null

    override suspend fun getAvailableObjects(types: List<GitObject.Type>?): Sequence<GitObjectInfo> = mutex.withLock {
        (
            database.objectQueries
                .list(repositoryIdentifier, (types ?: GitObject.Type.entries).map { it.ordinal.toLong() })
                .awaitAsList()
                .asSequence()
                .mapNotNull { obj ->
                    if (objects.containsKey(obj.hash)) {
                        return@mapNotNull null
                    }
                    return@mapNotNull GitObjectInfo(obj.hash, obj.type.toGitObjectType())
                }
            + objects.asSequence().map { GitObjectInfo(it.key, it.value.type) }
        )
    }

    override suspend fun hasObjects(hashes: List<String>): List<Boolean> {
        val ret: MutableList<Boolean> = mutableListOf()
        val neededHashes: MutableMap<String, Int> = mutableMapOf()

        for ((index, hash) in hashes.withIndex()) {
            val hasObject: Boolean = objects.contains(hash)
            ret.add(hasObject)

            if (!hasObject) {
                neededHashes[hash] = index
            }
        }

        if (neededHashes.isNotEmpty()) {
            val databaseHashes: List<String> =
                database.objectQueries
                    .listHashes(repositoryIdentifier, neededHashes.keys)
                    .awaitAsList()

            for (hash in databaseHashes) {
                val index: Int = neededHashes[hash]!!
                ret[index] = true
            }
        }

        return ret
    }

    override suspend fun readObjectOrNull(hash: String): GitObject? = mutex.withLock {
        objects[hash]?.obj
        ?: database.objectQueries.get(repositoryIdentifier, hash).awaitAsOneOrNull()?.let { (dataBase64, type) ->
            GitObject(base64.decode(dataBase64), type.toGitObjectType(), hash)
        }
    }

    override suspend fun readObjects(hashes: List<String>): List<GitObject> {
        val ret: MutableList<GitObject> = mutableListOf()
        val neededHashes: MutableList<String> = mutableListOf()
        for (hash in hashes) {
            val obj: GitObject? = objects[hash]?.obj
            if (obj != null) {
                ret.add(obj)
            }
            else {
                neededHashes.add(hash)
            }
        }

        if (neededHashes.isNotEmpty()) {
            ret.addAll(
                database.objectQueries.getMultiple(repositoryIdentifier, neededHashes).awaitAsList().map { (hash, type, dataBase64) ->
                    GitObject(base64.decode(dataBase64), type.toGitObjectType(), hash)
                }
            )
        }

        return ret
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
