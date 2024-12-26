package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.runtime.Composable
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_loader_button_proceed
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.`database_loader_finished_$duration_s_$warnings_$errors`
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_loader_proceed_tooltip_load_in_progress
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_loader_title
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStep
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStepCheckIfUpToDate
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStepLoadOnline
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.OfflineDatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import dev.toastbits.lifelog.application.dbsource.domain.model.LogDatabaseParseResult
import dev.toastbits.lifelog.core.specification.converter.alert.LogConvertAlert
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import korlibs.math.roundDecimalPlaces
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

class DatabaseSourceLoadScreen(
    sourceConfiguration: DatabaseSourceConfiguration,
    private val onLoaded: (LogDatabase) -> Unit,
    autoProceed: Boolean = false
): DatabaseSourceProcessScreen<LogDatabaseParseResult>(
    sourceConfiguration = sourceConfiguration,
    autoProceed = autoProceed,
    textProvider = DatabaseSourceLoadScreenTextProvider
) {
    override val title: String
        @Composable
        get() = stringResource(Res.string.database_loader_title)

    override fun getInitialStep(databaseAccessor: DatabaseAccessor): LoadStep<LogDatabaseParseResult> =
        if (databaseAccessor is OfflineDatabaseAccessor)
            LoadStepCheckIfUpToDate(databaseAccessor)
        else
            LoadStepLoadOnline

    override fun canProceedWithResult(result: LogDatabaseParseResult): Boolean =
        result.alerts.none { it.alert.severity == LogConvertAlert.Severity.ERROR }

    override fun getResultAlerts(result: LogDatabaseParseResult): List<Alert> =
        result.alerts.map {
            Alert(
                message = it.alert.toString(),
                filePath = it.filePath,
                lineIndex = it.lineIndex,
                severity =
                    when (it.alert.severity) {
                        LogConvertAlert.Severity.ERROR -> Alert.Severity.ERROR
                        LogConvertAlert.Severity.WARNING -> Alert.Severity.WARNING
                    }
            )
        }

    override fun hasUserProceedAction(): Boolean = true

    override fun onUserProceeded(result: LogDatabaseParseResult) {
        onLoaded(result.database)
    }
}

private data object DatabaseSourceLoadScreenTextProvider: DatabaseSourceProcessScreenTextProvider {
    @Composable
    override fun getFinishedText(
        warnings: List<Alert>,
        errors: List<Alert>,
        duration: Duration
    ): String =
        stringResource(Res.string.`database_loader_finished_$duration_s_$warnings_$errors`)
            .replace("\$duration_s", (duration.inWholeMilliseconds / 1000f).roundDecimalPlaces(2).toString())
            .replace("\$warnings", warnings.size.toString())
            .replace("\$errors", errors.size.toString())

    @Composable
    override fun getProcessingTooltip(): String =
        stringResource(Res.string.database_loader_proceed_tooltip_load_in_progress)
    
    @Composable
    override fun getProceedButton(): String =
        stringResource(Res.string.database_loader_button_proceed)
}
