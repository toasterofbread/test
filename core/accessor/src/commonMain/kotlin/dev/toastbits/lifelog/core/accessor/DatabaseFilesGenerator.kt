package dev.toastbits.lifelog.core.accessor

import dev.toastbits.kogit.core.filestructure.FileStructure
import dev.toastbits.lifelog.core.specification.converter.GenerateAlertData
import dev.toastbits.lifelog.core.specification.database.LogDatabase

interface DatabaseFilesGenerator {
    fun generateDatabaseFileStructure(database: LogDatabase, onAlert: (GenerateAlertData) -> Unit): FileStructure
}
