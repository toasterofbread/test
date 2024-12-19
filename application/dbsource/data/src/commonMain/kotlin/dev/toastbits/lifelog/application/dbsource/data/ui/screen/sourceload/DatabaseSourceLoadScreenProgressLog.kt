package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.platform.composable.ScrollBarLazyColumn
import dev.toastbits.composekit.components.utils.composable.wave.WaveLineArea
import dev.toastbits.composekit.theme.core.ThemeValues
import dev.toastbits.composekit.theme.core.ui.LocalComposeKitTheme
import dev.toastbits.lifelog.application.core.ui.LinkText
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.button_database_loader_go_to_file
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.`database_loader_finished_$duration_$warnings_$errors`
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

@Composable
internal fun DatabaseSourceLoadScreenProgressLog(
    result: Pair<List<Alert>, Duration>?,
    databaseAccessor: DatabaseAccessor,
    finishedStepsProgress: List<DatabaseAccessor.LoadProgress>,
    currentProgress: DatabaseAccessor.LoadProgress?,
    loadException: Throwable?,
    modifier: Modifier = Modifier
) {
    val theme: ThemeValues = LocalComposeKitTheme.current
    val scrollState: LazyListState = rememberLazyListState()

    LaunchedEffect(result) {
        if (result != null) {
            scrollState.scrollToItem(Int.MAX_VALUE)
        }
    }

    val (warnings: List<Alert>, errors: List<Alert>) =
        remember(result?.first) {
            result?.first?.let { alerts ->
                alerts.filter { it.severity == Alert.Severity.WARNING } to alerts.filter { it.severity == Alert.Severity.ERROR }
            } ?: Pair(emptyList(), emptyList())
        }

    val loadExceptionStackTrace: List<String>? = remember(loadException) {
        loadException?.stackTraceToString()?.split('\n')
    }

    WaveLineArea(
        modifier,
        periodMillis = 3000
    ) {
        SelectionContainer {
            ScrollBarLazyColumn(
                state = scrollState,
                contentPadding = PaddingValues(10.dp),
                columnModifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(finishedStepsProgress) { loadProgress ->
                    LoadProgressDisplay(loadProgress, done = true)
                }

                currentProgress?.also { progress ->
                    item {
                        LoadProgressDisplay(progress, done = false)
                    }
                }

                loadExceptionStackTrace?.also { exceptionLines ->
                    items(exceptionLines) { line ->
                        Text(
                            line,
                            color = theme.error
                        )
                    }
                }

                result?.also { (alerts, duration) ->
                    items(alerts) { alert ->
                        AlertLine(alert, databaseAccessor)
                    }

                    item {
                        Text(
                            stringResource(Res.string.`database_loader_finished_$duration_$warnings_$errors`)
                                .replace("\$duration", duration.toString())
                                .replace("\$warnings", warnings.size.toString())
                                .replace("\$errors", errors.size.toString()),
                            Modifier.padding(top = 15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertLine(
    alert: Alert,
    databaseAccessor: DatabaseAccessor
) {
    val theme: ThemeValues = LocalComposeKitTheme.current

    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(color = theme.error)
    ) {
        Row {
            // TODO | Localise
            Text(
                when (alert.severity) {
                    Alert.Severity.ERROR -> "Error"
                    Alert.Severity.WARNING -> "Warning"
                    Alert.Severity.UNKNOWN -> "Unknown"
                }
            )

            if (alert.filePath != null) {
                Text(" at ")
                LinkText(
                    text = alert.filePath.toString() + alert.lineIndex?.let { ":$it" }.orEmpty(),
                    url = remember(alert) { alert.getUri(databaseAccessor) },
                    linkContentDescription = stringResource(Res.string.button_database_loader_go_to_file)
                )
            }

            Text(" | ${alert.message}")
        }
    }
}

private fun Alert.getUri(databaseAccessor: DatabaseAccessor): String? =
    filePath?.let { databaseAccessor.getFileLineUri(it, lineIndex) }
