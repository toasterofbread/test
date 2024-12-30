package dev.toastbits.lifelog.application.logview.screen

import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseSaver
import dev.toastbits.lifelog.core.specification.database.LogDatabase

fun interface LogSaveScreenProvider<T: LogDatabase> {
    operator fun invoke(
        database: T,
        autoProceed: Boolean,
        onProceeded: ((DatabaseSaver.SaveResult<T>) -> Unit)?,
        onSaveFinished: (DatabaseSaver.SaveResult<T>) -> Unit
    ): Screen
}
