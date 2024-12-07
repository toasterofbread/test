package dev.toastbits.lifelog.application.logview.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.platform.composable.ScrollBarLazyColumn
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.composekit.theme.vibrantAccent
import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.application.logview.model.LogEventReference
import dev.toastbits.lifelog.core.specification.converter.generateUserContent
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import io.github.petertrr.diffutils.diff
import io.github.petertrr.diffutils.patch.Patch
import io.github.petertrr.diffutils.text.DiffRow
import io.github.petertrr.diffutils.text.DiffRowGenerator
import lifelog.application.logview.generated.resources.Res
import lifelog.application.logview.generated.resources.`log_view_screen_$x_content_changes_made`
import org.jetbrains.compose.resources.pluralStringResource

@Composable
fun LogEventChangesDiff(
    event: LogEvent,
    eventReference: LogEventReference,
    changes: LogEntityChanges<LogEvent>,
    logDatabase: LogDatabase,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    scrollBarContentPadding: PaddingValues = contentPadding
) {
    val contentChanges: Pair<Patch<String>, List<DiffRow>>? =
        remember(event, changes) {
            val newContent: UserContent =
                changes.firstWithPropertyOrNull(LogEvent.PROPERTY_CONTENT)?.newValue
                ?: return@remember null

            val a: String = logDatabase.converter.generateUserContent(event.content ?: UserContent.EMPTY, eventReference.date)
            val b: String = logDatabase.converter.generateUserContent(newContent, eventReference.date)

            return@remember (
                diff(a, b) to DiffRowGenerator(
                    inlineDiffByWord = true
                ).generateDiffRows(
                    a.split('\n'),
                    b.split('\n')
                )
            )
        }

    Column(modifier) {
        if (contentChanges != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val (patch: Patch<String>, diff: List<DiffRow>) = contentChanges
                Text(
                    pluralStringResource(Res.plurals.`log_view_screen_$x_content_changes_made`, patch.deltas.size)
                        .replace("\$x", patch.deltas.size.toString()),
                    style = MaterialTheme.typography.titleSmall
                )

                Diff(
                    diff,
                    contentPadding = contentPadding,
                    scrollBarContentPadding = scrollBarContentPadding
                )
            }
        }
    }
}

@Composable
private fun Diff(
    diff: List<DiffRow>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    scrollBarContentPadding: PaddingValues = contentPadding
) {
    val theme: ThemeValues = LocalComposeKitTheme.current
    val inactiveTextColour = theme.onBackground.copy(alpha = 0.5f)

    ScrollBarLazyColumn(
        modifier,
        contentPadding = contentPadding,
        scrollBarContentPadding = scrollBarContentPadding
    ) {
        item {
            Column {
                for (row in diff) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val oldColour: Color
                        val newColour: Color

                        when (row.tag) {
                            DiffRow.Tag.DELETE -> {
                                oldColour = theme.error
                                newColour = theme.onBackground
                            }
                            DiffRow.Tag.INSERT,
                            DiffRow.Tag.CHANGE -> {
                                oldColour = theme.onBackground
                                newColour = theme.vibrantAccent
                            }
                            DiffRow.Tag.EQUAL -> {
                                oldColour = inactiveTextColour
                                newColour = inactiveTextColour
                            }
                        }

                        SelectionContainer(
                            Modifier.fillMaxWidth(0.5f)
                        ) {
                            Text(
                                row.oldLine.replace("<br/>", "\n"),
                                color = oldColour
                            )
                        }
                        SelectionContainer(
                            Modifier.fillMaxWidth()
                        ) {
                            Text(
                                row.newLine.replace("<br/>", "\n"),
                                color = newColour
                            )
                        }
                    }
                }
            }
        }
    }
}
