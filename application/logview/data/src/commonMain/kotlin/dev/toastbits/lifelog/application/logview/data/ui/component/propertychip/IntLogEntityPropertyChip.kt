package dev.toastbits.lifelog.application.logview.data.ui.component.propertychip

import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.toastbits.composekit.components.utils.composable.animatedvisibility.NullableValueAnimatedVisibility
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.util.AlertDialog
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.property.IntLogEntityProperty

@Composable
internal fun <T: LogEntity> IntLogEntityPropertyChip(
    name: String,
    value: Int?,
    entity: T,
    property: IntLogEntityProperty<T>,
    onEdit: ((Int?) -> Unit)?,
    modifier: Modifier = Modifier
) {
    var showInputDialog: Boolean by remember { mutableStateOf(false) }
    val inputDialogInput: TextFieldState = remember { TextFieldState(value?.toString().orEmpty()) }

    val currentInput: Int? = inputDialogInput.text.toString().toIntOrNull()
    val inputValid: Boolean = currentInput != null && property.isValueAllowed(entity, currentInput)

    LocalNavigator.current.AlertDialog(
        show = showInputDialog,
        onDismissRequest = { showInputDialog = false },
        confirmButton = {
            Button(
                {
                    if (inputValid) {
                        onEdit?.invoke(currentInput)
                    }
                },
                enabled = inputValid
            ) {
                Text("Set // TODO")
            }
        },
        dismissButton = {
            Button({ showInputDialog = false }) {
                Text("Cancel // TODO")
            }
        },
        title = {
            Text("Set $name // TODO")
        },
        text = {
            Column {
                TextField(
                    inputDialogInput,
                    isError = !inputValid,
                    label =
                        if (inputValid) null
                        else {{
                            if (currentInput == null) {
                                Text("Input is not an integer // TODO")
                            }
                            else if (currentInput !in property.allowedRange) {
                                Text("Input is not in range ${property.allowedRange} // TODO")
                            }
                            else {
                                Text("Unknown error // TODO")
                            }
                        }}
                )

                Button({
                    onEdit?.invoke(null)
                    showInputDialog = false
                }) {
                    Text("Set to null // TODO")
                }
            }
        }
    )

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("$name: $value")

        NullableValueAnimatedVisibility(
            onEdit,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) { lambda ->
            if (lambda == null) {
                return@NullableValueAnimatedVisibility
            }

            Row {
                IconButton({ lambda((value ?: 0) + 1) }) {
                    Icon(Icons.Default.Add, null) // TODO
                }
                IconButton({ lambda((value ?: 0) - 1) }) {
                    Icon(Icons.Default.Remove, null) // TODO
                }
                IconButton({ showInputDialog = true }) {
                    Icon(Icons.Default.Edit, null) // TODO
                }
            }
        }
    }
}
