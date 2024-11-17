package dev.toastbits.lifelog.application.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.StandardButton
import dev.toastbits.composekit.settings.ui.screen.PreferencesGroupScreen
import dev.toastbits.composekit.settings.ui.screen.PreferencesTopScreen
import dev.toastbits.composekit.utils.composable.animatedvisibility.NullableValueAnimatedVisibility
import dev.toastbits.lifelog.application.settings.data.compositionlocal.LocalSettings
import dev.toastbits.lifelog.application.settings.data.ui.screen.AppSettingsScreen
import dev.toastbits.lifelog.application.settings.domain.appsettings.AppSettings
import lifelog.application.app.generated.resources.Res
import lifelog.application.app.generated.resources.button_navigate_back
import lifelog.application.app.generated.resources.button_open_settings
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PersistentTopBar(modifier: Modifier) {
    val navigator: Navigator = LocalNavigator.current
    val settings: AppSettings = LocalSettings.current

    Row(
        modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            { navigator.navigateBackward() },
            enabled = navigator.getNavigateBackwardCount() > 0
        ) {
            Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, stringResource(Res.string.button_navigate_back))
        }

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            NullableValueAnimatedVisibility(
                navigator.currentTitle,
                Modifier.clipToBounds(),
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) { title ->
                if (title != null) {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineMedium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
        }

        Crossfade(navigator.currentExtraButtons) { buttons ->
            for (button in buttons) {
                button.StandardButton()
            }
        }

        val inSettings: Boolean =
            navigator.getMostRecentOfOrNull {
                it is PreferencesTopScreen || it is PreferencesGroupScreen
            } != null

        AnimatedVisibility(!inSettings) {
            IconButton({
                navigator.pushScreen(AppSettingsScreen(settings), skipIfSameClass = true)
            }) {
                Icon(Icons.Default.Settings, stringResource(Res.string.button_open_settings))
            }
        }
    }
}
