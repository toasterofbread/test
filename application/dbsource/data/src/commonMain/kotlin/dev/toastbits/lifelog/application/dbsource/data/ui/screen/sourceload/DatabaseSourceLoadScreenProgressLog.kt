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
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.`database_processor_alert_$message_$severity`
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.`database_processor_alert_$message_$severity_$location`
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_processor_alert_severity_error
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_processor_alert_severity_unknown
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_processor_alert_severity_warning
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import dev.toastbits.lifelog.application.usercontent.UserContentDisplay
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.MarkdownUserContentParser
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

@Composable
internal fun DatabaseSourceLoadScreenProgressLog(
    alerts: List<Alert>,
    finishDuration: Duration?,
    warnings: List<Alert>,
    errors: List<Alert>,
    databaseAccessor: DatabaseAccessor,
    textProvider: DatabaseSourceProcessScreenTextProvider,
    finishedStepsProgress: List<DatabaseAccessor.LoadProgress>,
    currentProgress: DatabaseAccessor.LoadProgress?,
    loadException: Throwable?,
    modifier: Modifier = Modifier
) {
    val theme: ThemeValues = LocalComposeKitTheme.current
    val scrollState: LazyListState = rememberLazyListState()

    LaunchedEffect(finishDuration) {
        if (finishDuration != null) {
            scrollState.scrollToItem(Int.MAX_VALUE)
        }
    }

    val loadExceptionStackTrace: List<String>? =
        remember(loadException) {
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
                        LoadProgressDisplay(progress, done = loadException != null)
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

                items(alerts) { alert ->
                    AlertLine(alert, databaseAccessor)
                }

                if (finishDuration != null) {
                    item {
                        Text(
                            textProvider.getFinishedText(warnings, errors, finishDuration),
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
            val severityText: String =
                when (alert.severity) {
                    Alert.Severity.ERROR -> stringResource(Res.string.database_processor_alert_severity_error)
                    Alert.Severity.WARNING -> stringResource(Res.string.database_processor_alert_severity_warning)
                    Alert.Severity.UNKNOWN -> stringResource(Res.string.database_processor_alert_severity_unknown)
                }

            val locationText: String =
                remember(alert) { alert.filePath.toString() + alert.lineIndex?.let { ":$it" }.orEmpty() }
            val locationUri: String? =
                remember(alert) { alert.getUri(databaseAccessor) }
            
            val baseText: String = (
                if (alert.filePath != null)
                    stringResource(Res.string.`database_processor_alert_$message_$severity_$location`)
                        .replace("\$location", locationText.toMarkdownLink(locationUri))
                else 
                    stringResource(Res.string.`database_processor_alert_$message_$severity`)
            )

            val userContent: UserContent =
                remember(alert, baseText, severityText) {
                    val text: String =
                        baseText
                            .replace("\$message", alert.message)
                            .replace("\$severity", severityText)

                    return@remember MarkdownUserContentParser.parseUserContent(
                        text,
                        object : LogEntityReferenceParser {
                            override fun parseReference(
                                text: String,
                                onAlert: (LogParseAlert) -> Unit
                            ): LogEntityReference =
                                LogEntityReference.URL(text)
                        },
                        { _, _ -> }
                    )
                }

            UserContentDisplay(
                userContent
            ) {
                Row {
                    it()
                }
            }
        }
    }
}

private fun String.toMarkdownLink(target: String?): String =
    if (target == null) this
    else "[$this]($target)"

private fun Alert.getUri(databaseAccessor: DatabaseAccessor): String? =
    filePath?.let { databaseAccessor.getFileLineUri(it, lineIndex) }
