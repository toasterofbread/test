package dev.toastbits.lifelog.core.accessor.reference

import dev.toastbits.kogit.core.filestructure.toPath
import dev.toastbits.lifelog.core.accessor.DatabaseFileStructureProvider
import dev.toastbits.lifelog.core.specification.converter.alert.LogGenerateAlert
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityPath
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import kotlinx.datetime.LocalDate
import okio.Path

class LogEntityReferenceGeneratorImpl(
    private val fileStructureProvider: DatabaseFileStructureProvider,
    private val relativeToDate: LocalDate
): LogEntityReferenceGenerator {
    override fun generateReferencePath(
        reference: LogEntityReference,
        relativeToOverride: LogEntityPath?,
        onAlert: (LogGenerateAlert) -> Unit
    ): LogEntityPath {
        val basePath: Path =
            if (relativeToOverride != null) relativeToOverride.segments.toPath()
            else fileStructureProvider.getLogFilePath(relativeToDate).parent!!

        val referencePath: Path = fileStructureProvider.getEntityReferenceFilePath(reference)
        if (basePath.isRoot) {
            return LogEntityPath(referencePath.segments)
        }

        return LogEntityPath(referencePath.relativeTo(basePath).segments)
    }
}
