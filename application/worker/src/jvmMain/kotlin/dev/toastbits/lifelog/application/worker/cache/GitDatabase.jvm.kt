package dev.toastbits.lifelog.application.worker.cache

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.lifelog.application.worker.GitDatabase
import java.io.File

internal actual suspend fun GitDatabase.Companion.createInstance(context: PlatformContext): GitDatabase {
    val databaseFile: File = context.getFilesDir()!!.file.resolve("GitDatabase.db")
    databaseFile.parentFile.mkdirs()

    val databaseExists: Boolean = databaseFile.isFile
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:" + databaseFile.absolutePath)

    if (!databaseExists) {
        GitDatabase.Schema.awaitCreate(driver)
    }

    return GitDatabase(driver)
}
