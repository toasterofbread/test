package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.LocalContext
import dev.toastbits.composekit.components.platform.composable.ScrollBarLazyColumn
import dev.toastbits.composekit.components.utils.composable.wave.WaveLineArea
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.composekit.theme.vibrantAccent
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.converter.alert.LogConvertAlert
import lifelog.application.dbsource.data.generated.resources.Res
import lifelog.application.dbsource.data.generated.resources.button_database_loader_go_to_file
import lifelog.application.dbsource.data.generated.resources.`database_loader_finished_$duration_$warnings_$errors`
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

@Composable
internal fun DatabaseSourceLoadScreenProgressLog(
    result: Pair<List<LogFileConverter.AlertOnLine<*>>, Duration>?,
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

    val (warnings, errors) = remember(result?.first) {
        result?.first?.let { alerts ->
            alerts.filter { it.alert.severity == LogConvertAlert.Severity.WARNING } to alerts.filter { it.alert.severity == LogConvertAlert.Severity.ERROR }
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
    alert: LogFileConverter.AlertOnLine<*>,
    databaseAccessor: DatabaseAccessor
) {
    val context: PlatformContext = LocalContext.current
    val theme: ThemeValues = LocalComposeKitTheme.current

    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(
            color =
                when (alert.alert.severity) {
                    LogConvertAlert.Severity.WARNING -> theme.onBackground
                    LogConvertAlert.Severity.ERROR -> theme.error
                }
        )
    ) {
        Row {
            Text(
                when (alert.alert.severity) {
                    LogConvertAlert.Severity.WARNING -> "Warning"
                    LogConvertAlert.Severity.ERROR -> "Error"
                } + " at "
            )

            val buttonDatabaseLoaderGoToFile: String =
                stringResource(Res.string.button_database_loader_go_to_file)
            val url: String? =
                remember(alert) {
                    alert.getUri(databaseAccessor)?.takeIf { context.canOpenUrl() }
                }

            Text(
                alert.filePath.toString() + alert.lineIndex?.let { ":$it" }.orEmpty(),
                color =
                    if (url != null) theme.vibrantAccent
                    else Color.Unspecified,
                modifier =
                    if (url != null)
                        Modifier
                            .clickable {
                                context.openUrl(url)
                            }
                            .semantics {
                                contentDescription = buttonDatabaseLoaderGoToFile
                                role = Role.Button
                            }
                            .pointerHoverIcon(PointerIcon.Hand, true)
                    else Modifier
            )

            Text(" | $alert")
        }
    }
}

private fun LogFileConverter.AlertOnLine<*>.getUri(databaseAccessor: DatabaseAccessor): String? =
    filePath?.let { databaseAccessor.getFileLineUri(it, lineIndex) }
