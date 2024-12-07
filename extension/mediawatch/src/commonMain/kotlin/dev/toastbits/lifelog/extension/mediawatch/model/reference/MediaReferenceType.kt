package dev.toastbits.lifelog.extension.mediawatch.model.reference

import dev.toastbits.lifelog.core.specification.converter.ParseAlertData
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.converter.alert.SpecificationLogParseAlert
import dev.toastbits.lifelog.core.specification.database.LogDataFile
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceType
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtension
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtensionStrings
import dev.toastbits.lifelog.extension.mediawatch.impl.model.mapper.createReference
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType

class MediaReferenceType(
    private val strings: MediaWatchExtensionStrings
): LogEntityReferenceType.InMetadata() {
    override val id: String get() = strings.mediaReferenceTypeId
    override val extensionId: ExtensionId = MediaWatchExtension.ID

    override fun parseReference(
        path: List<String>,
        onAlert: (LogParseAlert) -> Unit
    ): LogEntityReference.InMetadata? {
        val (entityType: MediaEntityType, mediaId: String) = parsePath(path, onAlert) ?: return null
        return entityType.createReference(mediaId, strings.mediaReferenceTypeId)
    }

    override fun parseReferenceMetadata(
        path: List<String>,
        lines: Sequence<String>,
        onAlert: (ParseAlertData) -> Unit
    ): LogDataFile? {
        val (entityType: MediaEntityType, mediaId: String) =
            parsePath(path) { onAlert(ParseAlertData(it, null, null)) } ?: return null

        // TODO
        return LogDataFile.Lines(listOf("TODO $entityType $mediaId"))
    }

    private fun parsePath(path: List<String>, onAlert: (LogParseAlert) -> Unit): Pair<MediaEntityType, String>? {
        if (path.size != 2) {
            onAlert(SpecificationLogParseAlert.InvalidReferenceSize(path, 2))
            return null
        }

        val entityTypeName: String? = path.firstOrNull()?.lowercase()
        val entityType: MediaEntityType? = MediaEntityType.entries.firstOrNull { it.name.lowercase() == entityTypeName }

        if (entityType == null) {
            onAlert(SpecificationLogParseAlert.UnknownReferenceType(path, 0))
            return null
        }

        return Pair(entityType, path[1])
    }
}
