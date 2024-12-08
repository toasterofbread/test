package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.lifelog.application.dbsource.data.ui.component.DatabaseSourceConfigurationPreview
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import lifelog.application.dbsource.data.generated.resources.Res
import lifelog.application.dbsource.data.generated.resources.button_database_loader_cancel
import lifelog.application.dbsource.data.generated.resources.button_database_loader_proceed
import lifelog.application.dbsource.data.generated.resources.database_loader_proceed_tooltip_errors_must_be_resolved
import lifelog.application.dbsource.data.generated.resources.database_loader_proceed_tooltip_load_in_progress
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

@Composable
internal fun <R> DatabaseSourceProcessor(
    sourceConfiguration: DatabaseSourceConfiguration,
    databaseAccessor: DatabaseAccessor,
    loadException: Throwable?,
    finishedStepsProgress: MutableList<DatabaseAccessor.LoadProgress>,
    currentProgress: DatabaseAccessor.LoadProgress?,
    loadResult: Pair<R, Duration>?,
    getAlerts: (R) -> List<Alert>,
    modifier: Modifier = Modifier,
    onUserProceeded: ((R) -> Unit)?,
    autoProceed: Boolean = false,
    canProceedWith: (R) -> Boolean = { true }
) {
    val navigator: Navigator = LocalNavigator.current
    val theme: ThemeValues = LocalComposeKitTheme.current

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DatabaseSourceConfigurationPreview(sourceConfiguration)

        DatabaseSourceLoadScreenProgressLog(
            result = loadResult?.let { (result, duration) -> getAlerts(result) to duration },
            databaseAccessor = databaseAccessor,
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
            val allowProceed: Boolean =
                remember(loadResult) {
                    loadResult?.first?.let { canProceedWith(it) } ?: false
                }

            Button({ navigator.navigateBackward() }) {
                val proceedTextOpacity: Float by animateFloatAsState(if (onUserProceeded == null && allowProceed) 1f else 0f)

                Box(contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.button_database_loader_cancel),
                        Modifier.graphicsLayer { alpha = 1f - proceedTextOpacity }
                    )
                    Text(
                        stringResource(Res.string.button_database_loader_proceed),
                        Modifier.graphicsLayer { alpha = proceedTextOpacity }
                    )
                }
            }

            if (autoProceed || onUserProceeded == null) {
                return@Row
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

                        loadResult?.first?.also {
                            onUserProceeded(it)
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
