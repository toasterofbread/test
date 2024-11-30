package dev.toastbits.lifelog.core.accessor.reference

import com.eygraber.uri.UriCodec
import dev.toastbits.lifelog.core.accessor.DatabaseFileStructureProvider
import dev.toastbits.lifelog.core.specification.converter.alert.LogGenerateAlert
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityPath
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import okio.Path

class LogEntityReferenceGeneratorImpl(
    private val fileStructureProvider: DatabaseFileStructureProvider
): LogEntityReferenceGenerator {
    override fun generateReferencePath(
        reference: LogEntityReference,
        relativeToOverride: LogEntityPath?,
        onAlert: (LogGenerateAlert) -> Unit
    ): LogEntityPath {
        val referencePath: Path = fileStructureProvider.getEntityReferenceFilePath(reference)
        return LogEntityPath(referencePath.segments.map { UriCodec.decode(it) })
    }
}
