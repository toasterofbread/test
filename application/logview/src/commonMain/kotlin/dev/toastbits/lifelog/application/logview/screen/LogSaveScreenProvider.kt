package dev.toastbits.lifelog.application.logview.screen

import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseSaver
import dev.toastbits.lifelog.core.specification.database.LogDatabase

fun interface LogSaveScreenProvider {
    operator fun invoke(
        database: LogDatabase,
        autoProceed: Boolean,
        onProceeded: ((DatabaseSaver.SaveResult) -> Unit)?,
        onSaveFinished: (DatabaseSaver.SaveResult) -> Unit
    ): Screen
}
