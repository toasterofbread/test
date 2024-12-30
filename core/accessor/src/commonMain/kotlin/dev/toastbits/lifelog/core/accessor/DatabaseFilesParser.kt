package dev.toastbits.lifelog.core.accessor

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.lifelog.core.specification.converter.ParseAlertData
import dev.toastbits.lifelog.core.specification.database.LogDatabaseData

interface DatabaseFilesParser {
    suspend fun parseDatabaseFileStructure(
        structure: FileStructure,
        onAlert: (ParseAlertData) -> Unit
    ): LogDatabaseData
}
