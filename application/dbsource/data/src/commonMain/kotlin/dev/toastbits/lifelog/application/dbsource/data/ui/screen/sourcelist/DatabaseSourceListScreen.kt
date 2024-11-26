package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourcelist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceconfiguration.DatabaseSourceConfigurationScreen
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.DatabaseSourceLoadScreen
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceType
import dev.toastbits.lifelog.application.logview.data.ui.screen.TopLogViewScreen
import dev.toastbits.lifelog.application.settings.data.compositionlocal.LocalSettings
import dev.toastbits.lifelog.application.settings.domain.appsettings.AppSettings
import dev.toastbits.lifelog.application.settings.domain.model.SerialisedDatabaseSourceConfiguration
import dev.toastbits.lifelog.application.settings.domain.model.deserialiseConfiguration
import dev.toastbits.lifelog.application.settings.domain.model.serialiseConfiguration
import lifelog.application.dbsource.data.generated.resources.Res
import lifelog.application.dbsource.data.generated.resources.button_configure_database_source_cancel
import lifelog.application.dbsource.data.generated.resources.button_configure_database_source_save
import lifelog.application.dbsource.data.generated.resources.button_new_database_source_add
import lifelog.application.dbsource.data.generated.resources.button_new_database_source_cancel
import org.jetbrains.compose.resources.stringResource

class DatabaseSourceListScreen: Screen {
    @Composable
    override fun Content(navigator: Navigator, modifier: Modifier, contentPadding: PaddingValues) {
        val settings: AppSettings = LocalSettings.current
        val autoOpenIndex: Int by settings.DatabaseSource.AUTO_OPEN_SOURCE_INDEX.observe()

        var serialisedSourceConfigurations: List<SerialisedDatabaseSourceConfiguration> by settings.DatabaseSource.DATABASE_SOURCES.observe()
        val sourceConfigurations: List<DatabaseSourceConfiguration> = remember(serialisedSourceConfigurations) {
            serialisedSourceConfigurations.map { serialised ->
                settings.DatabaseSource.sourceTypeRegistry.deserialiseConfiguration(serialised)
            }
        }

        val sourceTypes: List<DatabaseSourceType<*>> = settings.DatabaseSource.sourceTypeRegistry.getAll().values.toList()

        DatabaseSourceList(
            sourceConfigurations,
            sourceTypes,
            modifier,
            contentPadding = contentPadding,
            autoOpenConfigurationIndex = autoOpenIndex,
            onSelected = { index ->
                val source: DatabaseSourceConfiguration = sourceConfigurations[index]
                navigator.pushScreen(
                    DatabaseSourceLoadScreen(
                        source,
                        onLoaded = { database ->
                            navigator.replaceScreen(TopLogViewScreen(database))
                        }
                    )
                )
            },
            onDisableAutoOpenRequested = {
                settings.DatabaseSource.AUTO_OPEN_SOURCE_INDEX.set(-1)
            },
            onRemoveRequested = { index ->
                serialisedSourceConfigurations = serialisedSourceConfigurations.toMutableList().apply { removeAt(index) }
            },
            onEditRequested = { index ->
                val source: DatabaseSourceConfiguration = sourceConfigurations[index]
                navigator.pushScreen(
                    DatabaseSourceConfigurationScreen(
                        source,
                        index,
                        onSaved = { configuration, autoOpen ->
                            val serialised: SerialisedDatabaseSourceConfiguration = settings.DatabaseSource.sourceTypeRegistry.serialiseConfiguration(configuration)
                            settings.DatabaseSource.DATABASE_SOURCES.set(serialisedSourceConfigurations.toMutableList().apply { set(index, serialised) })

                            if (autoOpen) {
                                settings.DatabaseSource.AUTO_OPEN_SOURCE_INDEX.set(index)
                            }
                            else if (settings.DatabaseSource.AUTO_OPEN_SOURCE_INDEX.get() == index) {
                                settings.DatabaseSource.AUTO_OPEN_SOURCE_INDEX.reset()
                            }

                            navigator.navigateBackward()
                        },
                        onCancelled = {
                            navigator.navigateBackward()
                        },
                        getSaveText = { stringResource(Res.string.button_configure_database_source_save) },
                        getCancelText = { stringResource(Res.string.button_configure_database_source_cancel) }
                    )
                )
            },
            onTypeAddRequested = { index ->
                val type: DatabaseSourceType<*> = sourceTypes[index]
                navigator.pushScreen(
                    DatabaseSourceConfigurationScreen(
                        type.createNewConfiguration(),
                        serialisedSourceConfigurations.size,
                        onSaved = { configuration, autoOpen ->
                            val newItemIndex: Int = serialisedSourceConfigurations.size
                            val serialised: SerialisedDatabaseSourceConfiguration = settings.DatabaseSource.sourceTypeRegistry.serialiseConfiguration(configuration)
                            settings.DatabaseSource.DATABASE_SOURCES.set(serialisedSourceConfigurations + serialised)

                            if (autoOpen) {
                                settings.DatabaseSource.AUTO_OPEN_SOURCE_INDEX.set(newItemIndex)
                            }

                            navigator.navigateBackward()
                        },
                        onCancelled = {
                            navigator.navigateBackward()
                        },
                        getSaveText = { stringResource(Res.string.button_new_database_source_add) },
                        getCancelText = { stringResource(Res.string.button_new_database_source_cancel) }
                    )
                )
            }
        )
    }
}
