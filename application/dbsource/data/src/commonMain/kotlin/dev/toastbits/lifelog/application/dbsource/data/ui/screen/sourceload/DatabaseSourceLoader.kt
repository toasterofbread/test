package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.lifelog.application.dbsource.data.ui.component.DatabaseSourceConfigurationPreview
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStep
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStepCheckIfUpToDate
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStepLoadOnline
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.OfflineDatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.model.LogDatabaseParseResult
import dev.toastbits.lifelog.core.specification.converter.alert.LogConvertAlert
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import lifelog.application.dbsource.data.generated.resources.Res
import lifelog.application.dbsource.data.generated.resources.button_database_loader_cancel
import lifelog.application.dbsource.data.generated.resources.button_database_loader_proceed
import lifelog.application.dbsource.data.generated.resources.database_loader_proceed_tooltip_errors_must_be_resolved
import lifelog.application.dbsource.data.generated.resources.database_loader_proceed_tooltip_load_in_progress
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@Composable
internal fun DatabaseSourceLoader(
    sourceConfiguration: DatabaseSourceConfiguration,
    databaseAccessor: DatabaseAccessor,
    modifier: Modifier = Modifier,
    onProceeded: (LogDatabase) -> Unit,
    autoProceed: Boolean = false
) {
    val navigator: Navigator = LocalNavigator.current
    val theme: ThemeValues = LocalComposeKitTheme.current

    var loadException: Throwable? by remember { mutableStateOf(null) }
    var currentStep: LoadStep =
        remember(databaseAccessor) {
            if (databaseAccessor is OfflineDatabaseAccessor) LoadStepCheckIfUpToDate(
                databaseAccessor
            )
            else LoadStepLoadOnline
        }

    val finishedStepsProgress: MutableList<DatabaseAccessor.LoadProgress> = remember { mutableStateListOf() }
    var currentProgress: DatabaseAccessor.LoadProgress? by remember { mutableStateOf(null) }

    val loadStartTime: TimeMark = remember { TimeSource.Monotonic.markNow() }
    var loadResult: Pair<LogDatabaseParseResult, Duration>? by remember { mutableStateOf(null) }

    LaunchedEffect(currentStep) {
        val result: LoadStep.ExecuteResult =
            currentStep.execute(databaseAccessor) { progress ->
                if (progress.isError) {
                    finishedStepsProgress.add(progress)
                    return@execute
                }

                val current: DatabaseAccessor.LoadProgress? = currentProgress
                if (current != null && current.getMessageResource() != progress.getMessageResource()) {
                    finishedStepsProgress.add(current)
                }
                currentProgress = progress
            }

        currentProgress?.also {
            finishedStepsProgress.add(it)
            currentProgress = null
        }

        when (result) {
            is LoadStep.ExecuteResult.DatabaseLoaded -> {
                if (result.parseResult.alerts.isEmpty() || autoProceed) {
                    onProceeded(result.parseResult.database)
                }
                else {
                    loadResult = result.parseResult to loadStartTime.elapsedNow()
                }
            }
            is LoadStep.ExecuteResult.ExceptionThrown -> {
                result.exception.printStackTrace()
                loadException = result.exception
            }

            is LoadStep.ExecuteResult.NextStep -> currentStep = result.nextStep
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DatabaseSourceConfigurationPreview(sourceConfiguration)

        DatabaseSourceLoadScreenProgressLog(
            result = loadResult,
            finishedStepsProgress = finishedStepsProgress,
            currentProgress = currentProgress,
            loadException = loadException,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
        ) {
            Button({ navigator.navigateBackward() }) {
                Text(stringResource(Res.string.button_database_loader_cancel))
            }

            if (autoProceed) {
                return@Row
            }

            val allowProceed: Boolean = remember(loadResult) {
                loadResult?.first?.alerts?.none { it.alert.severity == LogConvertAlert.Severity.ERROR } == true
            }

            TooltipBox(
                TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip(
                        containerColor = theme.error
                    ) {
                        if (loadResult == null) {
                            Text(stringResource(Res.string.database_loader_proceed_tooltip_load_in_progress))
                        }
                        else {
                            Text(stringResource(Res.string.database_loader_proceed_tooltip_errors_must_be_resolved))
                        }
                    }
                },
                state = rememberTooltipState(),
                enableUserInput = !allowProceed
            ) {
                Button(
                    {
                        if (!allowProceed) {
                            return@Button
                        }

                        loadResult?.first?.database?.also {
                            onProceeded(it)
                        }
                    },
                    enabled = allowProceed
                ) {
                    Text(stringResource(Res.string.button_database_loader_proceed))
                }
            }
        }
    }
}
