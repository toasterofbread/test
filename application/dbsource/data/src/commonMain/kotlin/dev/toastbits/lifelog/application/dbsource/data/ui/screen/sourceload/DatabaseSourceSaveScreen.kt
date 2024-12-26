package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.runtime.Composable
import dev.toastbits.kogit.memory.handler.GitCommitGenerator.UserInfo
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_saver_button_proceed
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.`database_saver_finished_$duration_s_$warnings_$errors`
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_saver_proceed_tooltip_save_in_progress
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_saver_title
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStep
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStepSaveOnline
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseSaver.SaveResult
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import korlibs.math.roundDecimalPlaces
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

internal class DatabaseSourceSaveScreen(
    private val database: LogDatabase,
    private val message: String,
    private val author: UserInfo,
    private val committer: UserInfo,
    sourceConfiguration: DatabaseSourceConfiguration,
    private val onProceeded: ((SaveResult) -> Unit)?,
    private val onSaveFinished: (SaveResult) -> Unit,
    autoProceed: Boolean = false
): DatabaseSourceProcessScreen<SaveResult>(
    sourceConfiguration = sourceConfiguration,
    autoProceed = autoProceed,
    textProvider = DatabaseSourceSaveScreenTextProvider,
    showProceedAndCancel = false
) {
    override val title: String
        @Composable
        get() = stringResource(Res.string.database_saver_title)

    override fun getInitialStep(databaseAccessor: DatabaseAccessor): LoadStep<SaveResult> =
        LoadStepSaveOnline(
            database,
            message,
            author,
            committer
        )

    override fun canProceedWithResult(result: SaveResult): Boolean =
        result is SaveResult.Success

    override fun getResultAlerts(result: SaveResult): List<Alert> =
        result.alerts

    override fun hasUserProceedAction(): Boolean = onProceeded != null

    override fun onUserProceeded(result: SaveResult) {
        onProceeded?.invoke(result)
    }

    override fun onProcessFinished(result: SaveResult) {
        onSaveFinished(result)
    }
}


private data object DatabaseSourceSaveScreenTextProvider: DatabaseSourceProcessScreenTextProvider {
    @Composable
    override fun getFinishedText(
        warnings: List<Alert>,
        errors: List<Alert>,
        duration: Duration
    ): String =
        stringResource(Res.string.`database_saver_finished_$duration_s_$warnings_$errors`)
            .replace("\$duration_s", (duration.inWholeMilliseconds / 1000f).roundDecimalPlaces(2).toString())
            .replace("\$warnings", warnings.size.toString())
            .replace("\$errors", errors.size.toString())

    @Composable
    override fun getProcessingTooltip(): String =
        stringResource(Res.string.database_saver_proceed_tooltip_save_in_progress)

    @Composable
    override fun getProceedButton(): String =
        stringResource(Res.string.database_saver_button_proceed)
}

