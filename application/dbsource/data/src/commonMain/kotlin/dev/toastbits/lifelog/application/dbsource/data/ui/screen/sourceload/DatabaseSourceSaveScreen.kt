package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.runtime.Composable
import dev.toastbits.kogit.memory.handler.GitCommitGenerator.UserInfo
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStep
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStepCheckIfUpToDate
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStepSaveOnline
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.SaveResult
import dev.toastbits.lifelog.application.dbsource.domain.accessor.OfflineDatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import lifelog.application.dbsource.data.generated.resources.Res
import lifelog.application.dbsource.data.generated.resources.database_saver_title
import org.jetbrains.compose.resources.stringResource

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
    autoProceed = autoProceed
) {
    override val title: String
        @Composable
        get() = stringResource(Res.string.database_saver_title)

    override fun getInitialStep(databaseAccessor: DatabaseAccessor): LoadStep<SaveResult> =
        if (databaseAccessor is OfflineDatabaseAccessor)
            LoadStepCheckIfUpToDate(databaseAccessor)
        else
            LoadStepSaveOnline(
                database,
                message,
                author,
                committer
            )

    override fun canProceedWithResult(result: SaveResult): Boolean =
        result.isSuccess

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
