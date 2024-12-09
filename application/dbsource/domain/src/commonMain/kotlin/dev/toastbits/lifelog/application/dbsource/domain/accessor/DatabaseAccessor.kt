package dev.toastbits.lifelog.application.dbsource.domain.accessor

import androidx.compose.runtime.Composable
import dev.toastbits.kogit.memory.handler.GitCommitGenerator.UserInfo
import dev.toastbits.kogit.memory.model.GitObjectRegistry
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import dev.toastbits.lifelog.application.dbsource.domain.model.LogDatabaseParseResult
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import okio.Path
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

interface DatabaseAccessor {
    suspend fun loadOnlineDatabase(onProgress: (LoadProgress) -> Unit): Result<LogDatabaseParseResult>
    suspend fun saveOnlineDatabase(
        database: LogDatabase,
        message: String,
        author: UserInfo,
        committer: UserInfo,
        onProgress: (LoadProgress) -> Unit
    ): Result<SaveResult>

    data class SaveResult(
        val isSuccess: Boolean,
        val alerts: List<Alert>
    )

    fun getFileLineUri(filePath: Path, lineIndex: UInt?): String?

    interface LoadProgress {
        val type: Type
        val isError: Boolean get() = false

        fun getMessageResource(): StringResource
        fun isUnique(): Boolean = false

        suspend fun getProgressMessage(): String? = null

        interface Absolute: LoadProgress {
            val progressFraction: Float
        }

        sealed interface Type {
            data object Generic: Type
            data object Network: Type
            data class Object(val currentObject: GitObjectRegistry.GitObjectInfo): Type
        }

        companion object
    }
}

interface OfflineDatabaseAccessor: DatabaseAccessor {
    val offlineLocationName: String
        @Composable get

    suspend fun checkIfUpToDate(): Result<Boolean>
    suspend fun loadOfflineDatabase(): Result<LogDatabaseParseResult>
}

fun DatabaseAccessor.LoadProgress.Companion.message(resource: StringResource): DatabaseAccessor.LoadProgress =
    object : DatabaseAccessor.LoadProgress {
        override val type: LoadProgress.Type = LoadProgress.Type.Generic
        override fun getMessageResource(): StringResource = resource
        override fun isUnique(): Boolean = true
    }

fun DatabaseAccessor.LoadProgress.Companion.error(
    resource: StringResource,
    getMessage: suspend (StringResource) -> String = { getString(it) }
): DatabaseAccessor.LoadProgress =
    object : DatabaseAccessor.LoadProgress {
        override val type: LoadProgress.Type = LoadProgress.Type.Generic
        override val isError: Boolean = true
        override fun getMessageResource(): StringResource = resource
        override suspend fun getProgressMessage(): String = getMessage(resource)
        override fun isUnique(): Boolean = true
    }
