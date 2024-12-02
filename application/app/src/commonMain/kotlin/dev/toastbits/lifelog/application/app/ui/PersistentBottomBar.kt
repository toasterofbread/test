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
import lifelog.application.app.generated.resources.Res
import lifelog.application.app.generated.resources.app_footer_author_prefix
import lifelog.application.app.generated.resources.app_footer_author_suffix
import lifelog.application.app.generated.resources.app_footer_body
import lifelog.application.app.generated.resources.author_name
import lifelog.application.app.generated.resources.author_url
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PersistentBottomBar(modifier: Modifier = Modifier) {
    val navigator: Navigator = LocalNavigator.current

    SelectionContainer(modifier) {
        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text(stringResource(Res.string.app_footer_author_prefix), softWrap = false)
                    LinkText(
                        stringResource(Res.string.author_name),
                        stringResource(Res.string.author_url),
                        softWrap = false
                    )
                    Text(stringResource(Res.string.app_footer_author_suffix), softWrap = false)
                }
                Text(stringResource(Res.string.app_footer_body), softWrap = true)
            }

            navigator.currentInfo?.also {
                Text(it)
            }
        }
    }
}
