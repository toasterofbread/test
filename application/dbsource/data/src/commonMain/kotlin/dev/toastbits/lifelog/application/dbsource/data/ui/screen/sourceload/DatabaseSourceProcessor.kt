package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.utils.composable.LoadActionButton
import dev.toastbits.composekit.components.utils.composable.RowOrColumn
import dev.toastbits.composekit.components.utils.composable.animatedvisibility.NullableValueAnimatedVisibility
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.theme.core.ThemeValues
import dev.toastbits.composekit.theme.core.ui.LocalComposeKitTheme
import dev.toastbits.composekit.util.thenIf
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_processor_button_retry
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_processor_tooltip_errors_must_be_resolved
import dev.toastbits.lifelog.application.dbsource.data.ui.component.DatabaseSourceConfigurationPreview
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

private val MIN_COLUMN_HEIGHT: Dp = 400.dp
private val LOG_MIN_WIDTH: Dp = 400.dp
private val SOURCE_MIN_WIDTH: Dp = 250.dp

@Composable
internal fun <R, T: LogDatabase> DatabaseSourceProcessor(
    sourceConfiguration: DatabaseSourceConfiguration<*>,
    databaseAccessor: DatabaseAccessor<T>,
    textProvider: DatabaseSourceProcessScreenTextProvider,
    loadException: Throwable?,
    finishedStepsProgress: MutableList<DatabaseAccessor.LoadProgress>,
    currentProgress: DatabaseAccessor.LoadProgress?,
    loadResult: Pair<R, Duration>?,
    alerts: List<Alert>,
    modifier: Modifier = Modifier,
    onUserProceeded: ((R) -> Unit)?,
    autoProceed: Boolean = false,
    onRetry: (suspend () -> Unit)? = null,
    canProceedWith: (R) -> Boolean = { true },
    showProceedAndCancel: Boolean = true,
    cancel: suspend (Navigator) -> Unit
) {
    val navigator: Navigator = LocalNavigator.current
    val theme: ThemeValues = LocalComposeKitTheme.current

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val (warnings: List<Alert>, errors: List<Alert>) =
            remember(alerts) {
                alerts.filter { it.severity == Alert.Severity.WARNING } to alerts.filter { it.severity == Alert.Severity.ERROR }
            }

        BoxWithConstraints(Modifier.fillMaxSize().weight(1f)) {
            val displayAsRow: Boolean = maxHeight < MIN_COLUMN_HEIGHT
            RowOrColumn(
                row = displayAsRow,
                modifier = Modifier.matchParentSize(),
                alignment = -1,
                arrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedVisibility(
                    !displayAsRow || (maxWidth - SOURCE_MIN_WIDTH) >= LOG_MIN_WIDTH,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    DatabaseSourceConfigurationPreview(
                        sourceConfiguration,
                        Modifier
                            .thenIf(displayAsRow) {
                                fillMaxHeight()
                                .width(SOURCE_MIN_WIDTH)
                            }
                    )
                }

                DatabaseSourceLoadScreenProgressLog(
                    alerts = alerts,
                    finishDuration = loadResult?.second,
                    warnings = warnings,
                    errors = errors,
                    databaseAccessor = databaseAccessor,
                    textProvider = textProvider,
                    finishedStepsProgress = finishedStepsProgress,
                    currentProgress = currentProgress,
                    loadException = loadException,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            }
        }

        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            val buttonPadding: PaddingValues = PaddingValues(horizontal = 5.dp)
            val allowProceed: Boolean =
                remember(loadResult) {
                    loadResult?.first?.let { canProceedWith(it) } ?: false
                }

            AnimatedVisibility(
                !allowProceed || showProceedAndCancel,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                LoadActionButton(
                    { cancel(navigator) },
                    modifier = Modifier.padding(buttonPadding)
                ) {
                    val proceedTextOpacity: Float by animateFloatAsState(if (onUserProceeded == null && allowProceed) 1f else 0f)

                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            textProvider.getCancelButton(),
                            Modifier.graphicsLayer { alpha = 1f - proceedTextOpacity },
                            softWrap = false
                        )
                        Text(
                            textProvider.getProceedButton(),
                            Modifier.graphicsLayer { alpha = proceedTextOpacity },
                            softWrap = false
                        )
                    }
                }
            }

            NullableValueAnimatedVisibility(
                onRetry.takeIf { loadException != null || errors.isNotEmpty() },
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) { retry ->
                if (retry == null) {
                    return@NullableValueAnimatedVisibility
                }

                LoadActionButton(
                    { retry() },
                    modifier = Modifier.padding(buttonPadding)
                ) {
                    Text(stringResource(Res.string.database_processor_button_retry))
                }
            }

            if (autoProceed || onUserProceeded == null) {
                return@FlowRow
            }

            TooltipBox(
                TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip(
                        containerColor = theme.error
                    ) {
                        if (loadResult == null) {
                            Text(textProvider.getProcessingTooltip())
                        }
                        else {
                            Text(stringResource(Res.string.database_processor_tooltip_errors_must_be_resolved))
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
                    modifier = Modifier.padding(buttonPadding),
                    enabled = allowProceed
                ) {
                    Text(
                        textProvider.getProceedButton(),
                        softWrap = false
                    )
                }
            }
        }
    }
}
