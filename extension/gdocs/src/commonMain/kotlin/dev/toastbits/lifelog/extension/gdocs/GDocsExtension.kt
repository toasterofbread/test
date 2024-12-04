package dev.toastbits.lifelog.extension.gdocs

import dev.toastbits.lifelog.core.accessor.extension.DatabaseFileStructureExtension
import dev.toastbits.lifelog.core.accessor.extension.DatabaseFileStructurePreprocessor
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.extension.gdocs.impl.GDocsDatabaseFileStructurePreprocessor
import dev.toastbits.lifelog.extension.gdocs.impl.GDocsExtensionStringsImpl

class GDocsExtension(
    val strings: GDocsExtensionStrings = GDocsExtensionStringsImpl(),
    preprocessor: GDocsDatabaseFileStructurePreprocessor = GDocsDatabaseFileStructurePreprocessor(strings)
): DatabaseFileStructureExtension {
    override val id: ExtensionId get() = strings.extensionId

    override val extraPreprocessors: List<DatabaseFileStructurePreprocessor> =
        listOf(preprocessor)
}
