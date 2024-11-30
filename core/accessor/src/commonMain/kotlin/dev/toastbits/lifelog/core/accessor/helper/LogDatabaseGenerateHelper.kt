package dev.toastbits.lifelog.core.accessor.helper

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.lifelog.core.accessor.DatabaseFileStructureProvider
import dev.toastbits.lifelog.core.accessor.DatabaseFilesGenerator
import dev.toastbits.lifelog.core.accessor.impl.DatabaseFileStructureProviderImpl
import dev.toastbits.lifelog.core.accessor.impl.DatabaseFilesGeneratorImpl
import dev.toastbits.lifelog.core.accessor.reference.LogEntityReferenceGeneratorImpl
import dev.toastbits.lifelog.core.specification.converter.GenerateAlertData
import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.impl.converter.LogFileConverterImpl

class LogDatabaseGenerateHelper(
    configuration: LogDatabaseConfiguration
) {
    private val fileStructureProvider: DatabaseFileStructureProvider =
        DatabaseFileStructureProviderImpl(configuration)
    private val converterImpl: LogFileConverter =
        LogFileConverterImpl(fileStructureProvider, { LogEntityReferenceGeneratorImpl(fileStructureProvider) }, configuration.strings, configuration.extensionRegistry)
    private val generator: DatabaseFilesGenerator =
        DatabaseFilesGeneratorImpl(converterImpl, fileStructureProvider, configuration.splitStrategy)

    fun generateFileStructure(
        database: LogDatabase,
        onAlert: (GenerateAlertData) -> Unit
    ): FileStructure =
        generator.generateDatabaseFileStructure(database, onAlert)
}
