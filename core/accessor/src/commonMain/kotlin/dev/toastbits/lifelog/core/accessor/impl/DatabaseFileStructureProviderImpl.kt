package dev.toastbits.lifelog.core.accessor.impl

import com.eygraber.uri.UriCodec
import dev.toastbits.kogit.core.filestructure.toPath
import dev.toastbits.lifelog.core.accessor.DatabaseFileStructureProvider
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.converter.alert.SpecificationLogParseAlert
import dev.toastbits.lifelog.core.specification.converter.validate
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityPath
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceType
import kotlinx.datetime.LocalDate
import okio.Path
import okio.Path.Companion.toPath

class DatabaseFileStructureProviderImpl(
    private val configuration: LogDatabaseConfiguration
): DatabaseFileStructureProvider {
    init {
        configuration.strings.validate()
    }

    override fun getLogFilePathSize(): Int = configuration.splitStrategy.componentsCount + 2

    override fun getLogFilePath(date: LocalDate): Path =
        (getLogFileDirectory(date) + listOf(configuration.strings.logFileName)).toPath()

    private fun getLogFileDirectory(date: LocalDate): List<String> =
        listOf(configuration.strings.logsDirectoryName) + configuration.splitStrategy.getDateComponents(date).map { it.toString().padStart(2, '0') }

    override fun getEntityReferenceFilePath(reference: LogEntityReference): Path {
        when (reference) {
            is LogEntityReference.InLog -> {
                val subdir: List<String> =
                    if (reference.extensionId == null) emptyList()
                    else listOf(configuration.strings.extensionContentDirectoryName, reference.extensionId.toString(), reference.referenceTypeId!!.toString())

                return (getLogFileDirectory(reference.logDate) + subdir + reference.path.segments).toPath()
            }
            is LogEntityReference.InMetadata -> {
                for (extension in configuration.extensionRegistry.getAllExtensions()) {
                    val referenceType: LogEntityReferenceType =
                        extension.extraInMetadataReferenceTypes.firstOrNull { it.extensionId == reference.extensionId } ?: continue

                    return (
                        listOf(
                            configuration.strings.metadataDirectoryName,
                            configuration.strings.extensionContentDirectoryName,
                            extension.id,
                            referenceType.id
                        ) + reference.path.segments.map { UriCodec.encode(it) }
                    ).toPath()
                }

                throw IllegalStateException("Could not find extension providing reference type for $this")
            }
            is LogEntityReference.URL -> throw IllegalStateException(reference.toString())
        }
    }

    override fun parseReference(
        text: String,
        onAlert: (LogParseAlert) -> Unit
    ): LogEntityReference? {
        if (URL_REGEX.matches(text)) {
            return LogEntityReference.URL(text)
        }

        val normalisedPath: List<String> =
            text.toPath().normalized().segments.map { UriCodec.decode(it) }

        if (normalisedPath.any { it == ".." }) {
            onAlert(SpecificationLogParseAlert.InvalidReferenceFormat(text))
            return null
        }

        when (normalisedPath.firstOrNull()) {
            configuration.strings.metadataDirectoryName -> {
                return parseMetadataReference(normalisedPath.drop(1), onAlert) {
                    onAlert(SpecificationLogParseAlert.UnknownReferenceType(normalisedPath, it + 1))
                }
            }
            configuration.strings.logsDirectoryName -> {
                return getPathLogFile(normalisedPath.drop(1)) { alert ->
                    if (alert is SpecificationLogParseAlert.UnknownReferenceType) {
                        onAlert(
                            SpecificationLogParseAlert.UnknownReferenceType(
                                normalisedPath,
                                alert.firstUnknownSegment + 1
                            )
                        )
                    }
                    else {
                        onAlert(alert)
                    }
                }
            }
            else -> {
                onAlert(SpecificationLogParseAlert.UnknownReferenceType(normalisedPath, 0))
                return null
            }
        }
    }

    private fun parseMetadataReference(path: List<String>, onAlert: (LogParseAlert) -> Unit, onFailure: (Int) -> Unit): LogEntityReference? {
        when (path.firstOrNull()) {
            configuration.strings.extensionContentDirectoryName -> {
                return parseExtensionMetadataReference(path.drop(1), onAlert) { onFailure(it + 1) }
            }
            else -> {
                onFailure(0)
                return null
            }
        }
    }

    private fun parseExtensionMetadataReference(path: List<String>, onAlert: (LogParseAlert) -> Unit, onFailure: (Int) -> Unit): LogEntityReference? {
        val extension: SpecificationExtension? = path.firstOrNull()?.let { configuration.extensionRegistry.findRegisteredExtension(it) }
        if (extension == null) {
            onFailure(0)
            return null
        }

        val referenceType: LogEntityReferenceType.InMetadata? = extension.extraInMetadataReferenceTypes.firstOrNull { it.id == path.getOrNull(1) }
        if (referenceType == null) {
            onFailure(1)
            return null
        }

        return referenceType.parseReference(path.drop(2)) { alert ->
            if (alert is SpecificationLogParseAlert.UnknownReferenceType) {
                onFailure(alert.firstUnknownSegment + 2)
            }
            else {
                onAlert(alert)
            }
        }
    }

    override fun getPathLogFile(path: List<String>, onAlert: (LogParseAlert) -> Unit): LogEntityReference.InLog? {
        if (path.size < configuration.splitStrategy.componentsCount) {
            onAlert(SpecificationLogParseAlert.UnknownReferenceType(path, 0))
            return null
        }

        val dateParts: List<Int> =
            path.take(configuration.splitStrategy.componentsCount).mapIndexed { index, part ->
                val int: Int? = part.toIntOrNull()
                if (int == null) {
                    onAlert(SpecificationLogParseAlert.UnknownReferenceType(path, index))
                    return null
                }
                return@mapIndexed int
            }

        val date: LocalDate = configuration.splitStrategy.parseDateComponents(dateParts)

        val extensionId: ExtensionId?
        val referenceTypeId: ExtensionId?

        var inLogPath: List<String> = path.drop(configuration.splitStrategy.componentsCount)
        if (inLogPath.firstOrNull() == configuration.strings.extensionContentDirectoryName) {
            if (inLogPath.size < 3) {
                onAlert(SpecificationLogParseAlert.UnknownReferenceType(path, configuration.splitStrategy.componentsCount))
                return null
            }

            extensionId = inLogPath[1]
            referenceTypeId = inLogPath[2]
            inLogPath = inLogPath.drop(3)
        }
        else {
            extensionId = null
            referenceTypeId = null
        }

        return LogEntityReference.InLogData(date, LogEntityPath(inLogPath), extensionId = extensionId, referenceTypeId = referenceTypeId)
    }

    companion object {
        private val URL_REGEX: Regex = "\\b[a-zA-Z][a-zA-Z0-9+.-]*://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%?=~_|]".toRegex()
    }
}
