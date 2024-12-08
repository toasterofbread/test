package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourcelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.platform.composable.ScrollBarLazyColumn
import dev.toastbits.lifelog.application.dbsource.data.ui.component.DatabaseSourceConfigurationPreview
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceType
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.button_delete_database_source
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.button_delete_database_source_cancel
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.button_delete_database_source_confirm
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_source_list_no_sources_added
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.dialog_delete_database_source_title
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.edit_delete_database_source
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DatabaseSourceList(
    configurations: List<DatabaseSourceConfiguration>,
    types: List<DatabaseSourceType<*>>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    autoOpenConfigurationIndex: Int? = null,
    onSelected: ((Int) -> Unit)? = null,
    onRemoveRequested: ((Int) -> Unit)? = null,
    onEditRequested: ((Int) -> Unit)? = null,
    onTypeAddRequested: ((Int) -> Unit)? = null
) {
    var confirmingRemovalOfIndex: Int? by remember { mutableStateOf(null) }

    confirmingRemovalOfIndex?.also { index ->
        SourceRemovalConfirmationDialog(
            configurations[index],
            onConfirmed = {
                onRemoveRequested?.invoke(index)
                confirmingRemovalOfIndex = null
            },
            onCancelled = {
                confirmingRemovalOfIndex = null
            }
        )
    }

    ScrollBarLazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        if (types.isNotEmpty()) {
            item {
                DatabaseSourceListActions(
                    types,
                    onTypeSelected = onTypeAddRequested,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (configurations.isEmpty()) {
            item {
                Text(stringResource(Res.string.database_source_list_no_sources_added))
            }
        }
        else {
            itemsIndexed(configurations) { index, sourceConfiguration ->
                DatabaseSourceConfigurationPreview(
                    sourceConfiguration,
                    Modifier.fillMaxWidth(),
                    autoOpens = index == autoOpenConfigurationIndex,
                    onSelected = onSelected?.let { { it(index) } },
                    tailItems = {
                        if (onEditRequested != null) {
                            IconButton({ onEditRequested(index) }) {
                                Icon(Icons.Default.Edit, stringResource(Res.string.edit_delete_database_source))
                            }
                        }

                        if (onRemoveRequested != null) {
                            IconButton({ confirmingRemovalOfIndex = index }) {
                                Icon(Icons.Default.Delete, stringResource(Res.string.button_delete_database_source))
                            }
                        }
                    }
                )

            }
        }
    }
}

@Composable
private fun SourceRemovalConfirmationDialog(
    sourceConfiguration: DatabaseSourceConfiguration,
    onConfirmed: () -> Unit,
    onCancelled: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelled,
        confirmButton = {
            Button(onConfirmed) {
                Text(stringResource(Res.string.button_delete_database_source_confirm))
            }
        },
        dismissButton = {
            Button(onCancelled) {
                Text(stringResource(Res.string.button_delete_database_source_cancel))
            }
        },
        title = {
            Text(stringResource(Res.string.dialog_delete_database_source_title))
        },
        text = {
            DatabaseSourceConfigurationPreview(sourceConfiguration, Modifier.fillMaxWidth())
        }
    )
}
