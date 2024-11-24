package dev.toastbits.lifelog.application.worker.cache

import android.database.sqlite.SQLiteException
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.lifelog.application.worker.GitDatabase

internal actual suspend fun GitDatabase.Companion.createInstance(
    context: PlatformContext
): GitDatabase {
    @Suppress("UNCHECKED_CAST")
    val schema: SqlSchema<QueryResult.Value<Unit>> =
        GitDatabase.Schema as SqlSchema<QueryResult.Value<Unit>> // TEMP SqlDelight

    val driver: SqlDriver =
        AndroidSqliteDriver(
            schema,
            context.ctx,
            "GitDatabase.db",
            callback = object : AndroidSqliteDriver.Callback(schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.setForeignKeyConstraintsEnabled(true)
                }
            }
        )

    try {
        GitDatabase.Schema.awaitCreate(driver)
    }
    catch(e: SQLiteException) {
        if (e.message?.startsWith("table ") != true) {
            throw e
        }
    }

    return GitDatabase(driver)
}