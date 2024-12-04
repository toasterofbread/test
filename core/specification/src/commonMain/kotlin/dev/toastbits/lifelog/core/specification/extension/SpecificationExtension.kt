package dev.toastbits.lifelog.core.specification.extension

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEventType
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceType

typealias ExtensionId = String

interface SpecificationExtension {
    val id: ExtensionId

    val extraEventTypes: List<LogEventType> get() = emptyList()
    val extraInLogReferenceTypes: List<LogEntityReferenceType.InLog> get() = emptyList()
    val extraInMetadataReferenceTypes: List<LogEntityReferenceType.InMetadata> get() = emptyList()

    companion object {
        val EMPTY: SpecificationExtension =
            object : SpecificationExtension {
                override val id: ExtensionId get() = throw IllegalStateException()
            }
    }
}

fun SpecificationExtension.validate() {
    for (referenceType in extraInLogReferenceTypes + extraInMetadataReferenceTypes) {
        check(referenceType.id.isNotBlank()) { "Identifier for reference type $referenceType is blank" }
        check(referenceType.id.none { LogFileConverterStrings.ILLEGAL_PATH_CHARS.contains(it) }) { "Reference type identifier '${referenceType.id}' contains illegal character(s)" }
    }
}
