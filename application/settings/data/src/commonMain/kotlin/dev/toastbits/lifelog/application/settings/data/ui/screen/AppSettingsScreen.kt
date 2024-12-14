package dev.toastbits.lifelog.application.settings.data.ui.screen

import dev.toastbits.composekit.components.utils.composable.pane.model.InitialPaneRatioSource
import dev.toastbits.composekit.settings.ui.screen.PlatformSettingsScreen
import dev.toastbits.lifelog.application.settings.domain.appsettings.AppSettings

fun AppSettingsScreen(settings: AppSettings): PlatformSettingsScreen =
    PlatformSettingsScreen(
        settings.prefs,
        settings.allGroups,
        initialStartPaneRatioSource =
            InitialPaneRatioSource.Remembered(
                "settings.data.ui.screen.AppSettingsScreen",
                InitialPaneRatioSource.Ratio(0.4f)
            )
    )
