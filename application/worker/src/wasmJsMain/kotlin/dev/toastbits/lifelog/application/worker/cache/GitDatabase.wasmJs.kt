package dev.toastbits.lifelog.application.worker.cache

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import app.cash.sqldelight.driver.worker.WebWorkerException
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.lifelog.application.worker.GitDatabase
import org.w3c.dom.Worker

private fun createSqlJsWorker(): Worker =
    js("""new Worker(new URL("@toastbits/sqljs/sqljs.worker.js", import.meta.url))""")

internal actual suspend fun GitDatabase.Companion.createInstance(context: PlatformContext): GitDatabase {
    val worker: Worker = createSqlJsWorker()
    val driver: SqlDriver = WebWorkerDriver(worker)

    try {
        GitDatabase.Schema.awaitCreate(driver)
    }
    catch (e: WebWorkerException) {
        if (e.message != "{\"message\":\"SQLITE_ERROR: sqlite3 result code 1: table Object already exists\",\"name\":\"Error\"}") {
            throw e
        }
    }

    return GitDatabase(driver)
}
