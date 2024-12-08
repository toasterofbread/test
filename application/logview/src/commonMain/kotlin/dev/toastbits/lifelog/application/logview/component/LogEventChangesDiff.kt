package dev.toastbits.lifelog.application.logview.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.utils.composable.pane.ResizableSyncedSplitColumn
import dev.toastbits.composekit.components.utils.composable.pane.model.InitialPaneRatioSource
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.composekit.theme.vibrantAccent
import dev.toastbits.lifelog.application.logview.component.propertychip.PropertyChip
import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.application.logview.model.LogEventReference
import dev.toastbits.lifelog.core.specification.converter.generateUserContent
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import io.github.petertrr.diffutils.diff
import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import dev.toastbits.lifelog.application.logview.generated.resources.Res
import dev.toastbits.lifelog.application.logview.generated.resources.`log_view_screen_$x_content_changes_made`
import dev.toastbits.lifelog.application.logview.generated.resources.`log_view_screen_$x_property_changes_made`
import dev.toastbits.lifelog.application.logview.generated.resources.log_view_screen_column_title_new
import dev.toastbits.lifelog.application.logview.generated.resources.log_view_screen_column_title_old
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T: LogEvent> LogEventChangesDiff(
    event: T,
    eventReference: LogEventReference,
    changes: LogEntityChanges<T>,
    logDatabase: LogDatabase,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    scrollBarContentPadding: PaddingValues = contentPadding
) {
    val stages: List<DiffStage<T>> =
        remember(event, changes) {
            buildDiffStages(changes, logDatabase, event, eventReference)
        }

    SelectionContainer(modifier) {
        ResizableSyncedSplitColumn(
            items = stages,
            initialStartPaneRatioSource =
                InitialPaneRatioSource.Remembered(
                    "logview.component.LogEventChangesDiff",
                    InitialPaneRatioSource.Ratio(0.5f)
                ),
            contentPadding = contentPadding,
            scrollBarContentPadding = scrollBarContentPadding,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            getItemContentAlignment = {
                if (it is DiffStage.Heading<T>) Alignment.CenterStart
                else Alignment.TopStart
            }
        ) { isStart: Boolean, stage: DiffStage<T> ->
            when (stage) {
                is DiffStage.Title -> {
                    val titleStringResource: StringResource =
                        if (isStart) Res.string.log_view_screen_column_title_old
                        else Res.string.log_view_screen_column_title_new

                    Text(
                        stringResource(titleStringResource),
                        Modifier
                            .padding(vertical = 15.dp)
                            .alpha(0.7f),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                is DiffStage.Heading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isStart) {
                            val headingStringResource: PluralStringResource =
                                when (stage) {
                                    is DiffStage.Heading.Content -> Res.plurals.`log_view_screen_$x_content_changes_made`
                                    is DiffStage.Heading.Properties -> Res.plurals.`log_view_screen_$x_property_changes_made`
                                }

                            Text(
                                pluralStringResource(headingStringResource, stage.changeCount)
                                    .replace("\$x", stage.changeCount.toString()),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        HorizontalDivider()
                    }
                }
                is DiffStage.Spacer ->
                    Spacer(Modifier.heightIn(20.dp))
                is DiffStage.Change.Content ->
                    ContentChangeText(
                        stage.diffRow,
                        isOld = isStart
                    )
                is DiffStage.Change.Property ->
                    PropertyChangeText(
                        stage.change,
                        isOld = isStart,
                        entity = event,
                        configuration = logDatabase.configuration,
                        modifier = Modifier.fillMaxWidth()
                    )
            }
        }
    }
}

@Composable
private fun <T: LogEntity, V> PropertyChangeText(
    change: LogEntityChanges.Change<T, V>,
    isOld: Boolean,
    entity: T,
    configuration: LogDatabaseConfiguration,
    modifier: Modifier = Modifier
) {
    val modifiedEntity: T =
        if (isOld) entity
        else change.applyTo(entity)

    change.property.PropertyChip(
        entity = modifiedEntity,
        configuration = configuration,
        onEdit = null,
        modifier = modifier
    )
}

@Composable
private fun ContentChangeText(
    diffRow: DiffRow,
    isOld: Boolean,
    modifier: Modifier = Modifier
) {
    val theme: ThemeValues = LocalComposeKitTheme.current

    val text: String =
        remember(diffRow, isOld) {
            (
                if (isOld) diffRow.oldLine
                else diffRow.newLine
            ).replace("<br/>", "\n")
        }

    val textColour: Color =
        when (diffRow.tag) {
            DiffRow.Tag.DELETE ->
                if (isOld) theme.error
                else theme.onBackground

            DiffRow.Tag.INSERT,
            DiffRow.Tag.CHANGE ->
                if (isOld) theme.onBackground
                else theme.vibrantAccent

            DiffRow.Tag.EQUAL ->
                theme.onBackground.copy(alpha = 0.5f)
        }

    Text(text, modifier, color = textColour)
}

private sealed interface DiffStage<T: LogEntity> {
    class Title<T: LogEntity>: DiffStage<T>
    sealed interface Heading<T: LogEntity>: DiffStage<T> {
        val changeCount: Int
        data class Properties<T: LogEntity>(override val changeCount: Int): Heading<T>
        data class Content<T: LogEntity>(override val changeCount: Int): Heading<T>
    }
    sealed interface Change<T: LogEntity>: DiffStage<T> {
        data class Property<T: LogEntity>(val change: LogEntityChanges.Change<T, *>): Change<T>
        data class Content<T: LogEntity>(val diffRow: DiffRow): Change<T>
    }
    class Spacer<T: LogEntity>: DiffStage<T>
}

private fun <T : LogEvent> buildDiffStages(
    changes: LogEntityChanges<T>,
    logDatabase: LogDatabase,
    event: T,
    eventReference: LogEventReference,
): List<DiffStage<T>> =
    buildList {
        add(DiffStage.Title())

        val propertyChanges: List<LogEntityChanges.Change<T, *>> =
            changes.changesList.filter { it.property != LogEvent.PROPERTY_CONTENT }

        add(DiffStage.Heading.Properties(propertyChanges.size))
        for (change in propertyChanges) {
            add(DiffStage.Change.Property(change))
        }

        add(DiffStage.Spacer())

        val newContent: UserContent =
            changes.firstWithPropertyOrNull(LogEvent.PROPERTY_CONTENT)?.newValue
                ?: return@buildList

        val a: String =
            logDatabase.converter.generateUserContent(event.content ?: UserContent.EMPTY, eventReference.date)
        val b: String =
            logDatabase.converter.generateUserContent(newContent, eventReference.date)

        val changeCount: Int = diff(a, b).deltas.size
        add(DiffStage.Heading.Content(changeCount))

        if (changeCount > 0) {
            val diffRowGenerator: DiffRowGenerator =
                DiffRowGenerator(inlineDiffByWord = true)

            val rows: List<DiffRow> =
                diffRowGenerator.generateDiffRows(
                    a.split('\n'),
                    b.split('\n')
                )

            for (row in rows) {
                add(DiffStage.Change.Content(row))
            }
        }
    }
