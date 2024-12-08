package dev.toastbits.lifelog.application.settings.data.ui.screen

import dev.toastbits.composekit.components.utils.composable.pane.model.InitialPaneRatioSource
import dev.toastbits.composekit.settings.ui.screen.PlatformSettingsScreen
import dev.toastbits.lifelog.application.settings.domain.appsettings.AppSettings
import dev.toastbits.lifelog.application.settings.data.generated.resources.Res
import dev.toastbits.lifelog.application.settings.data.generated.resources.screen_app_settings
import org.jetbrains.compose.resources.stringResource

fun AppSettingsScreen(settings: AppSettings): PlatformSettingsScreen =
    PlatformSettingsScreen(
        settings.prefs,
        settings.allGroups,
        { stringResource(Res.string.screen_app_settings) },
        initialStartPaneRatioSource =
            InitialPaneRatioSource.Remembered(
                "settings.data.ui.screen.AppSettingsScreen",
                InitialPaneRatioSource.Ratio(0.4f)
            )
    )
