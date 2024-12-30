package dev.toastbits.lifelog.application.dbsource.domain.model

import dev.toastbits.lifelog.core.specification.converter.ParseAlertData
import dev.toastbits.lifelog.core.specification.database.LogDatabase

data class LogDatabaseParseResult<T: LogDatabase>(val database: T, val alerts: List<ParseAlertData>)
