package dev.toastbits.lifelog.application.settings.data.group

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.toastbits.composekit.settings.ComposeKitSettingsGroupImpl
import dev.toastbits.composekit.settings.PlatformSettings
import dev.toastbits.composekit.settingsitem.domain.PlatformSettingsProperty
import dev.toastbits.composekit.settingsitem.domain.SettingsItem
import dev.toastbits.composekit.settingsitem.presentation.ui.component.item.TextFieldSettingsItem
import dev.toastbits.composekit.settingsitem.presentation.ui.component.item.ToggleSettingsItem
import dev.toastbits.kogit.core.model.GitCredentials
import dev.toastbits.kogit.core.provider.GitCredentialsProvider
import dev.toastbits.kogit.system.util.SystemGitCredentialsProvider
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceTypeRegistry
import dev.toastbits.lifelog.application.dbsource.inmemorygit.type.InMemoryGitDatabaseSourceType
import dev.toastbits.lifelog.application.settings.data.generated.resources.Res
import dev.toastbits.lifelog.application.settings.data.generated.resources.pref_database_source_git_binary_path_description
import dev.toastbits.lifelog.application.settings.data.generated.resources.pref_database_source_git_binary_path_title
import dev.toastbits.lifelog.application.settings.data.generated.resources.pref_database_source_git_password_description
import dev.toastbits.lifelog.application.settings.data.generated.resources.pref_database_source_git_password_title
import dev.toastbits.lifelog.application.settings.data.generated.resources.pref_database_source_git_username_description
import dev.toastbits.lifelog.application.settings.data.generated.resources.pref_database_source_git_username_title
import dev.toastbits.lifelog.application.settings.data.generated.resources.pref_database_source_use_system_git_credentials_description
import dev.toastbits.lifelog.application.settings.data.generated.resources.pref_database_source_use_system_git_credentials_title
import dev.toastbits.lifelog.application.settings.data.generated.resources.prefs_group_database_source_description
import dev.toastbits.lifelog.application.settings.data.generated.resources.prefs_group_database_source_title
import dev.toastbits.lifelog.application.settings.domain.group.DatabaseSourceSettingsGroup
import dev.toastbits.lifelog.application.settings.domain.model.SerialisedDatabaseSourceConfiguration
import org.jetbrains.compose.resources.stringResource

open class DatabaseSourceSettingsGroupImpl(
    preferences: PlatformSettings
): ComposeKitSettingsGroupImpl("DATABASE_SOURCE", preferences), DatabaseSourceSettingsGroup {
    @Composable
    override fun getTitle(): String = stringResource(Res.string.prefs_group_database_source_title)

    @Composable
    override fun getDescription(): String = stringResource(Res.string.prefs_group_database_source_description)

    @Composable
    override fun getIcon(): ImageVector = Icons.Default.Cloud

    override val sourceTypeRegistry: DatabaseSourceTypeRegistry =
        MapDatabaseSourceTypeRegistry(
            mapOf(
                "InMemoryGit" to InMemoryGitDatabaseSourceType
            )
        )

    override val AUTO_OPEN_SOURCE_INDEX: PlatformSettingsProperty<Int> by property(
        getName = { throw IllegalStateException("Internal property") },
        getDescription = { throw IllegalStateException("Internal property") },
        getDefaultValue = { -1 }
    )

    override val DATABASE_SOURCES: PlatformSettingsProperty<List<SerialisedDatabaseSourceConfiguration>> by
        serialisableProperty(
            getName = { throw IllegalStateException("Internal property") },
            getDescription = { throw IllegalStateException("Internal property") },
            getDefaultValue = { emptyList() }
        )

    override val GIT_BINARY_PATH: PlatformSettingsProperty<String> by property(
        getName = { stringResource(Res.string.pref_database_source_git_binary_path_title) },
        getDescription = { stringResource(Res.string.pref_database_source_git_binary_path_description) },
        getDefaultValue = { "" }
    )

    override val USE_SYSTEM_GIT_CREDENTIALS: PlatformSettingsProperty<Boolean> by property(
        getName = { stringResource(Res.string.pref_database_source_use_system_git_credentials_title) },
        getDescription = { stringResource(Res.string.pref_database_source_use_system_git_credentials_description) },
        getDefaultValue = { true }
    )

    override val GIT_USERNAME: PlatformSettingsProperty<String> by
        property(
            getName = { stringResource(Res.string.pref_database_source_git_username_title) },
            getDescription = { stringResource(Res.string.pref_database_source_git_username_description) },
            getDefaultValue = { "" }
        )

    override val GIT_PASSWORD: PlatformSettingsProperty<String> by
        property(
            getName = { stringResource(Res.string.pref_database_source_git_password_title) },
            getDescription = { stringResource(Res.string.pref_database_source_git_password_description) },
            getDefaultValue = { "" }
        )

    // AUTO_OPEN_SOURCE_INDEX ana DATABASE_SOURCES edited in DatabaseSourceListScreen
    override fun getConfigurationItems(): List<SettingsItem> =
        listOfNotNull(
            TextFieldSettingsItem(GIT_BINARY_PATH).takeIf { true }, // TODO | Hide on appropriate systems
            ToggleSettingsItem(USE_SYSTEM_GIT_CREDENTIALS).takeIf { SystemGitCredentialsProvider != null },
            TextFieldSettingsItem(GIT_USERNAME),
            TextFieldSettingsItem(GIT_PASSWORD)
        )
}

fun DatabaseSourceSettingsGroup.createGitCredentialsProvider(): GitCredentialsProvider =
    object : GitCredentialsProvider {
        override suspend fun invoke(
            url: String,
            repositoryPath: String?,
            defaultGitBinaryPath: String?
        ): Result<GitCredentials?> {
            if (USE_SYSTEM_GIT_CREDENTIALS.get()) {
                SystemGitCredentialsProvider?.invoke(
                    url,
                    repositoryPath,
                    GIT_BINARY_PATH.get().ifBlank { defaultGitBinaryPath }
                )?.fold(
                    onSuccess = {
                        if (it != null) {
                            return Result.success(it)
                        }
                    },
                    onFailure = {
                        return Result.failure(it)
                    }
                )
            }

            val gitUsername: String = GIT_USERNAME.get()
            val gitPassword: String = GIT_PASSWORD.get()

            if (gitUsername.isNotBlank() && gitPassword.isNotBlank()) {
                return Result.success(GitCredentials(gitUsername, gitPassword))
            }

            return Result.success(null)
        }
    }

