package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import dev.toastbits.composekit.components.utils.composable.animatedvisibility.NullableValueAnimatedVisibility
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.theme.core.ThemeValues
import dev.toastbits.composekit.theme.core.ui.LocalComposeKitTheme
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_processor_button_retry
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_processor_tooltip_errors_must_be_resolved
import dev.toastbits.lifelog.application.dbsource.data.ui.component.DatabaseSourceConfigurationPreview
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

@Composable
internal fun <R> DatabaseSourceProcessor(
    sourceConfiguration: DatabaseSourceConfiguration,
    databaseAccessor: DatabaseAccessor,
    textProvider: DatabaseSourceProcessScreenTextProvider,
    loadException: Throwable?,
    finishedStepsProgress: MutableList<DatabaseAccessor.LoadProgress>,
    currentProgress: DatabaseAccessor.LoadProgress?,
    loadResult: Pair<R, Duration>?,
    getAlerts: (R) -> List<Alert>,
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
        DatabaseSourceConfigurationPreview(sourceConfiguration)

        val hasErrors: Boolean =
            DatabaseSourceLoadScreenProgressLog(
                result = loadResult?.let { (result, duration) -> getAlerts(result) to duration },
                databaseAccessor = databaseAccessor,
                textProvider = textProvider,
                finishedStepsProgress = finishedStepsProgress,
                currentProgress = currentProgress,
                loadException = loadException,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            val spacing: Dp = 10.dp
            val allowProceed: Boolean =
                remember(loadResult) {
                    loadResult?.first?.let { canProceedWith(it) } ?: false
                }

            AnimatedVisibility(
                !allowProceed || showProceedAndCancel,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                LoadActionButton({ cancel(navigator) }) {
                    val proceedTextOpacity: Float by animateFloatAsState(if (onUserProceeded == null && allowProceed) 1f else 0f)

                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            textProvider.getCancelButton(),
                            Modifier.graphicsLayer { alpha = 1f - proceedTextOpacity }
                        )
                        Text(
                            textProvider.getProceedButton(),
                            Modifier.graphicsLayer { alpha = proceedTextOpacity }
                        )
                    }
                }
            }

            NullableValueAnimatedVisibility(
                onRetry.takeIf { loadException != null || hasErrors },
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) { retry ->
                if (retry == null) {
                    return@NullableValueAnimatedVisibility
                }

                LoadActionButton(
                    retry,
                    Modifier.padding(start = spacing)
                ) {
                    Text(stringResource(Res.string.database_processor_button_retry))
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
                    Modifier.padding(start = spacing),
                    enabled = allowProceed
                ) {
                    Text(textProvider.getProceedButton())
                }
            }
        }
    }
}
