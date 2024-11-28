package dev.toastbits.lifelog.application.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.toastbits.composekit.navigation.compositionlocal.LocalNavigator
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.StandardButton
import dev.toastbits.composekit.settings.ui.screen.PlatformSettingsGroupScreen
import dev.toastbits.composekit.settings.ui.screen.PlatformSettingsScreen
import dev.toastbits.lifelog.application.core.ui.GenericTopBar
import dev.toastbits.lifelog.application.settings.data.compositionlocal.LocalSettings
import dev.toastbits.lifelog.application.settings.data.ui.screen.AppSettingsScreen
import dev.toastbits.lifelog.application.settings.domain.appsettings.AppSettings
import lifelog.application.app.generated.resources.Res
import lifelog.application.app.generated.resources.button_open_settings
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PersistentTopBar(modifier: Modifier) {
    val navigator: Navigator = LocalNavigator.current
    val settings: AppSettings = LocalSettings.current

    GenericTopBar(
        title = navigator.currentTitle,
        onBack = {
            navigator.navigateBackward()
        },
        modifier = modifier,
        extraContent = {
            Crossfade(navigator.currentExtraButtons) { buttons ->
                for (button in buttons) {
                    button.StandardButton()
                }
            }

            val inSettings: Boolean =
                navigator.getMostRecentOfOrNull {
                    it is PlatformSettingsScreen || it is PlatformSettingsGroupScreen
                } != null

            AnimatedVisibility(!inSettings) {
                IconButton({
                    navigator.pushScreen(AppSettingsScreen(settings), skipIfSameClass = true)
                }) {
                    Icon(Icons.Default.Settings, stringResource(Res.string.button_open_settings))
                }
            }
        }
    )
}
