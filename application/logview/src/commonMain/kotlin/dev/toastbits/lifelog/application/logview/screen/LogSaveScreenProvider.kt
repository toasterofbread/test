package dev.toastbits.lifelog.application.logview.screen

import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.core.specification.database.LogDatabase

fun interface LogSaveScreenProvider {
    operator fun invoke(
        database: LogDatabase,
        autoProceed: Boolean,
        onProceeded: ((DatabaseAccessor.SaveResult) -> Unit)?,
        onSaveFinished: (DatabaseAccessor.SaveResult) -> Unit
    ): Screen
}
