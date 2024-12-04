package dev.toastbits.lifelog.extension.gdocs.model.reference

import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.converter.alert.SpecificationLogParseAlert
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceType
import dev.toastbits.lifelog.extension.gdocs.MediaExtensionStrings
import kotlinx.datetime.LocalDate

class MediaReferenceType(
    val strings: MediaExtensionStrings
): LogEntityReferenceType.InLog() {
    override val id: String get() = strings.mediaReferenceTypeId
    override val extensionId: ExtensionId get() = strings.extensionId

    override fun parseReference(
        path: List<String>,
        date: LocalDate,
        onAlert: (LogParseAlert) -> Unit
    ): LogEntityReference.InLog? {
        if (path.size != 2) {
            onAlert(SpecificationLogParseAlert.InvalidReferenceSize(path, 2))
            return null
        }

        val mediaType: MediaReference.Type? = MediaReference.Type.entries.firstOrNull() {
            it.getGroup().equals(
                path.first(),
                ignoreCase = true
            )
        }
        if (mediaType == null) {
            onAlert(SpecificationLogParseAlert.UnknownReferenceType(path, 0))
            return null
        }

        val index: UInt? = path[1].toUIntOrNull()
        if (index == null) {
            onAlert(SpecificationLogParseAlert.UnknownReferenceType(path, 0))
            return null
        }

        return MediaReference(
            extensionId = extensionId,
            index = index,
            type = mediaType,
            logDate = date,
            strings = strings
        )
    }
}
