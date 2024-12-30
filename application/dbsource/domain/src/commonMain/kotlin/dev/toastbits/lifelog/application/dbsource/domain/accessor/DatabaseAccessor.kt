package dev.toastbits.lifelog.application.dbsource.domain.accessor

import dev.toastbits.kogit.memory.model.GitObjectInfo
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.model.LogDatabaseParseResult
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import okio.Path
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

interface DatabaseAccessor<T: LogDatabase> {
    val saver: DatabaseSaver<T>

    suspend fun loadOnlineDatabase(onProgress: (LoadProgress) -> Unit): Result<LogDatabaseParseResult<T>>

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
            data class Object(val currentObject: GitObjectInfo): Type
        }

        companion object
    }
}

fun LoadProgress.Companion.message(resource: StringResource): LoadProgress =
    object : LoadProgress {
        override val type: LoadProgress.Type = LoadProgress.Type.Generic
        override fun getMessageResource(): StringResource = resource
        override fun isUnique(): Boolean = true
    }

fun LoadProgress.Companion.error(
    resource: StringResource,
    getMessage: suspend (StringResource) -> String = { getString(it) }
): LoadProgress =
    object : LoadProgress {
        override val type: LoadProgress.Type = LoadProgress.Type.Generic
        override val isError: Boolean = true
        override fun getMessageResource(): StringResource = resource
        override suspend fun getProgressMessage(): String = getMessage(resource)
        override fun isUnique(): Boolean = true
    }
