package dev.toastbits.lifelog.application.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.lifelog.application.core.ui.LinkText
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PersistentBottomBar(modifier: Modifier = Modifier) {
    val navigator: Navigator = LocalNavigator.current

    SelectionContainer(modifier) {
        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            navigator.currentInfo?.also {
                Text(it)
            }
        }
    }
}
