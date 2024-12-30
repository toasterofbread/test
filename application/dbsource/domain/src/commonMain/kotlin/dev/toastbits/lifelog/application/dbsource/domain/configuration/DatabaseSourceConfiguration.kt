package dev.toastbits.lifelog.application.dbsource.domain.configuration

import androidx.compose.runtime.Composable
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceType
import dev.toastbits.lifelog.core.specification.database.LogDatabase

interface DatabaseSourceConfiguration<T: LogDatabase> {
    fun getType(): DatabaseSourceType<*, T>

    @Composable
    fun getPreviewTitle(): String

    @Composable
    fun getPreviewContent(): String

    @Composable
    fun getInvalidReasonMessages(): Map<Int, String>
}

@Suppress("UNCHECKED_CAST")
fun <C: DatabaseSourceConfiguration<T>, T: LogDatabase> C.castType(): DatabaseSourceType<C, T> =
    getType() as DatabaseSourceType<C, T>
