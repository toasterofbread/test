package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.kogit.memory.handler.GitCommitGenerator.UserInfo
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStep
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStepCheckIfUpToDate
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStepSaveOnline
import dev.toastbits.lifelog.application.dbsource.data.ui.util.rememberDatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.OfflineDatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import lifelog.application.dbsource.data.generated.resources.Res
import lifelog.application.dbsource.data.generated.resources.database_saver_title
import org.jetbrains.compose.resources.stringResource

class DatabaseSourceSaveScreen(
    private val database: LogDatabase,
    private val message: String,
    private val author: UserInfo,
    private val committer: UserInfo,
    private val sourceConfiguration: DatabaseSourceConfiguration,
    private val onFinished: () -> Unit,
    private val autoProceed: Boolean = false
): Screen {
    override val title: String
        @Composable
        get() = stringResource(Res.string.database_saver_title)

    @Composable
    override fun Content(navigator: Navigator, modifier: Modifier, contentPadding: PaddingValues) {
        val databaseAccessor: DatabaseAccessor = rememberDatabaseAccessor(sourceConfiguration)

        val initialStep: LoadStep<Unit> =
            remember(databaseAccessor) {
                if (databaseAccessor is OfflineDatabaseAccessor)
                    LoadStepCheckIfUpToDate(databaseAccessor)
                else
                    LoadStepSaveOnline(
                        database,
                        message,
                        author,
                        committer
                    )
            }

        DatabaseSourceLoader(
            sourceConfiguration = sourceConfiguration,
            databaseAccessor = databaseAccessor,
            initialStep = initialStep,
            getAlerts = { emptyList() },
            modifier = modifier.padding(contentPadding),
            onProceeded = {
                onFinished()
            },
            autoProceed = autoProceed
        )
    }
}
