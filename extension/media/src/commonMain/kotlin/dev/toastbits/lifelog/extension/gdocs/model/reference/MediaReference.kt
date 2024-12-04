package dev.toastbits.lifelog.extension.gdocs.model.reference

import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityPath
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.extension.gdocs.MediaExtensionStrings
import kotlinx.datetime.LocalDate

data class MediaReference(
    override val extensionId: ExtensionId?,
    val index: UInt,
    val type: Type,
    override val logDate: LocalDate,
    val strings: MediaExtensionStrings
): LogEntityReference.InLog {
    override val referenceTypeId: ExtensionId get() = strings.mediaReferenceTypeId

    enum class Type {
        IMAGE_PNG;

        fun getGroup(): String =
            when (this) {
                IMAGE_PNG -> "image"
            }

        fun getFileExtension(): String =
            when (this) {
                IMAGE_PNG -> ".png"
            }
    }

    override val path: LogEntityPath
        get() = LogEntityPath.of(type.getGroup(), index.toString() + type.getFileExtension())
}
