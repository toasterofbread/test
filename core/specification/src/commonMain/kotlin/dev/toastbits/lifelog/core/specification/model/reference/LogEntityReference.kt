package dev.toastbits.lifelog.core.specification.model.reference

import com.eygraber.uri.UriCodec
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import kotlinx.datetime.LocalDate

sealed interface LogEntityReference {
    val extensionId: ExtensionId?
    val referenceTypeId: ExtensionId?
    val path: LogEntityPath

    interface InLog: LogEntityReference {
        val logDate: LocalDate
    }
    interface InMetadata: LogEntityReference

    data class InLogData(
        override val logDate: LocalDate,
        override val path: LogEntityPath,
        override val extensionId: ExtensionId?,
        override val referenceTypeId: ExtensionId?
    ) : InLog

    data class InMetadataData(
        override val path: LogEntityPath,
        override val extensionId: ExtensionId,
        override val referenceTypeId: ExtensionId
    ) : InMetadata

    // TODO | Should probably be separate
    data class URL(val url: String): LogEntityReference {
        override val extensionId: ExtensionId? = null
        override val referenceTypeId: ExtensionId? = null
        override val path: LogEntityPath get() = throw NotImplementedError()
    }
}

data class LogEntityPath(val segments: List<String>) {
    override fun toString(): String =
        "/" + segments.joinToString("/") {
            UriCodec.encode(it)
        }

    companion object {
        val ROOT: LogEntityPath get() = of("/")

        fun of(vararg segments: String): LogEntityPath =
            LogEntityPath(segments.toList())
    }
}
