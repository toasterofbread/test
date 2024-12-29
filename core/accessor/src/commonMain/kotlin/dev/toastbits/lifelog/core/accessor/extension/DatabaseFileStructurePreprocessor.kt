package dev.toastbits.lifelog.core.accessor.extension

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.lifelog.core.accessor.DatabaseFileStructureProvider
import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.ParseAlertData
import dev.toastbits.lifelog.core.specification.extension.ExtensionRegistry
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.UserContentParser
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser

interface DatabaseFileStructurePreprocessor {
    suspend fun processDatabaseFileStructure(
        fileStructure: FileStructure,
        fileStructureProvider: DatabaseFileStructureProvider,
        userContentParser: UserContentParser,
        referenceParser: LogEntityReferenceParser,
        strings: LogFileConverterStrings,
        extensionRegistry: ExtensionRegistry,
        onAlert: (ParseAlertData) -> Unit
    ): FileStructure
}
