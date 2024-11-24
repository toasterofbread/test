package dev.toastbits.lifelog.core.accessor.helper

import dev.toastbits.lifelog.core.accessor.DatabaseFileStructureProvider
import dev.toastbits.lifelog.core.accessor.DatabaseFilesParser
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.accessor.impl.DatabaseFileStructureProviderImpl
import dev.toastbits.lifelog.core.accessor.impl.DatabaseFilesParserImpl
import dev.toastbits.lifelog.core.accessor.reference.LogEntityReferenceGeneratorImpl
import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.converter.ParseAlertData
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.impl.converter.LogFileConverterImpl
import kotlinx.coroutines.CoroutineDispatcher

class LogDatabaseParseHelper(
    configuration: LogDatabaseConfiguration,
    ioDispatcher: CoroutineDispatcher
) {
    private val fileStructureProvider: DatabaseFileStructureProvider =
        DatabaseFileStructureProviderImpl(configuration)
    private val converterImpl: LogFileConverter =
        LogFileConverterImpl(fileStructureProvider, { LogEntityReferenceGeneratorImpl(fileStructureProvider, it) }, configuration.strings, configuration.extensionRegistry)
    private val parser: DatabaseFilesParser =
        DatabaseFilesParserImpl(converterImpl, configuration, fileStructureProvider, ioDispatcher)

    suspend fun parseFileStructure(fileStructure: FileStructure, onAlert: (ParseAlertData) -> Unit): LogDatabase {
        return parser.parseDatabaseFileStructure(fileStructure, onAlert)
    }
}
