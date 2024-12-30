package dev.toastbits.lifelog.core.accessor.impl

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.kogit.core.filestructure.readLines
import dev.toastbits.kogit.core.filestructure.walkFiles
import dev.toastbits.lifelog.core.accessor.DatabaseFileStructureProvider
import dev.toastbits.lifelog.core.accessor.DatabaseFilesParser
import dev.toastbits.lifelog.core.accessor.extension.DatabaseFileStructureExtension
import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.converter.ParseAlertData
import dev.toastbits.lifelog.core.specification.converter.alert.SpecificationLogParseAlert
import dev.toastbits.lifelog.core.specification.database.LogDataFile
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.database.LogDatabaseData
import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.UserContentParser
import dev.toastbits.lifelog.core.specification.impl.model.entity.date.LogDateImpl
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.Path

class DatabaseFilesParserImpl(
    private val converter: LogFileConverter,
    private val configuration: LogDatabaseConfiguration,
    private val fileStructureProvider: DatabaseFileStructureProvider,
    private val userContentParser: UserContentParser,
    private val referenceParser: LogEntityReferenceParser,
    private val ioDispatcher: CoroutineDispatcher
): DatabaseFilesParser {
    override suspend fun parseDatabaseFileStructure(
        structure: FileStructure,
        onAlert: (ParseAlertData) -> Unit
    ): LogDatabaseData = withContext(ioDispatcher) {
        val days: MutableMap<LogDate, List<LogEvent>> = mutableMapOf()
        val data: MutableMap<LogEntityReference, LogDataFile> = mutableMapOf()

        val scope: Scope = Scope(days, data, onAlert)

        structure.preprocess(onAlert).walkFiles { file, path ->
            val reference: LogEntityReference =
                fileStructureProvider.parseReference(path.toString()) {
                    if (it is SpecificationLogParseAlert.UnknownReferenceType && it.firstUnknownSegment == 0) {
                        return@parseReference
                    }
                    onAlert(ParseAlertData(it, null, path))
                } ?: return@walkFiles

            when (reference) {
                is LogEntityReference.InLog -> {
                    scope.onInLogEntityReference(reference, file, path)
                }
                is LogEntityReference.InMetadata -> {
                    scope.onInMetadataEntityReference(reference, file, path)
                }
                is LogEntityReference.URL -> throw IllegalStateException(reference.toString())
            }
        }

        return@withContext LogDatabaseData(
            configuration,
            days = scope.days,
            data = scope.data,
            converter = converter
        )
    }

    private suspend fun FileStructure.preprocess(onAlert: (ParseAlertData) -> Unit): FileStructure {
        var structure: FileStructure = this
        for (extension in configuration.extensionRegistry.getAllExtensions()) {
            if (extension !is DatabaseFileStructureExtension) {
                continue
            }

            for (preprocessor in extension.extraPreprocessors) {
                structure = preprocessor.processDatabaseFileStructure(
                    fileStructure = structure,
                    fileStructureProvider = fileStructureProvider,
                    userContentParser = userContentParser,
                    referenceParser = referenceParser,
                    strings = configuration.strings,
                    extensionRegistry = configuration.extensionRegistry,
                    onAlert = onAlert
                )
            }
        }
        return structure
    }

    private suspend fun Scope.onInLogEntityReference(
        reference: LogEntityReference.InLog,
        file: FileStructure.Node.File,
        filePath: Path
    ) {
        if (reference.extensionId != null) {
            val dataFile: LogDataFile =
                when (file) {
                    is FileStructure.Node.File.FileLines -> LogDataFile.Lines(file.readLines().toList())
                    is FileStructure.Node.File.FileBytes -> file.readBytes().let { (bytes, range) -> LogDataFile.Bytes(bytes, range) }
                }

            data[reference] = dataFile
            return
        }

        when (reference.path.segments.lastOrNull()) {
            configuration.strings.logFileName -> {
                val lines: Sequence<String> = file.readLines()

                val log: LogFileConverter.ParseResult = converter.parseLogFile(lines, filePath, initialDate = LogDateImpl(reference.logDate, ambiguous = true))
                log.alerts.forEach(onAlert)

                for ((day, events) in log.days) {
                    val existingEvents: List<LogEvent> = days[day].orEmpty()
                    days[day] = existingEvents + events
                }
            }
            else -> TODO(reference.toString())
        }
    }

    private suspend fun Scope.onInMetadataEntityReference(reference: LogEntityReference.InMetadata, file: FileStructure.Node.File, path: Path) {
        if (data.containsKey(reference)) {
            onAlert(ParseAlertData(SpecificationLogParseAlert.RedefinedMetadataValue(reference), null, path))
        }

        val extension: SpecificationExtension? = configuration.extensionRegistry.findRegisteredExtension(reference.extensionId!!)
        if (extension == null) {
            onAlert(ParseAlertData(SpecificationLogParseAlert.UnregisteredExtension(reference.extensionId!!), null, path))
            return
        }

        val referenceType: LogEntityReferenceType.InMetadata? = extension.extraInMetadataReferenceTypes.firstOrNull { it.id == reference.referenceTypeId }
        if (referenceType == null) {
            onAlert(ParseAlertData(
                SpecificationLogParseAlert.UnregisteredReferenceType(
                    reference.referenceTypeId,
                    reference.extensionId
                ), null, path))
            return
        }

        val lines: Sequence<String> = file.readLines()
        val parsedMetadata: LogDataFile =
            referenceType.parseReferenceMetadata(reference.path.segments, lines) { onAlert(it.copy(filePath = path)) }
            ?: return

        data[reference] = parsedMetadata
    }

    private class Scope(
        val days: MutableMap<LogDate, List<LogEvent>>,
        val data: MutableMap<LogEntityReference, LogDataFile>,
        val onAlert: (ParseAlertData) -> Unit
    )
}
