package dev.toastbits.lifelog.application.dbsource.inmemorygit.configuration

import androidx.compose.runtime.Composable
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.type.DatabaseSourceType
import dev.toastbits.lifelog.application.dbsource.inmemorygit.type.InMemoryGitDatabaseSourceType
import kotlinx.serialization.Serializable
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.source_configuration_repository_url_not_http
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.source_configuration_repository_url_not_set
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.source_configuration_branch_not_set
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.`source_configuration_preview_content_$name_$repositoryUrl_$branch`
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.`source_configuration_preview_title_$name_$repositoryUrl_$branch`
import org.jetbrains.compose.resources.stringResource

@Serializable
data class InMemoryGitDatabaseSourceConfiguration(
    val name: String = "",
    val repositoryUrl: String = "",
    val branchName: String = ""
): DatabaseSourceConfiguration {
    override fun getType(): DatabaseSourceType<InMemoryGitDatabaseSourceConfiguration> = InMemoryGitDatabaseSourceType

    @Composable
    override fun getPreviewTitle(): String = (
        stringResource(Res.string.`source_configuration_preview_title_$name_$repositoryUrl_$branch`)
            .replaceStringKey("\$name", name)
            .replaceStringKey("\$repositoryUrl", repositoryUrl)
            .replaceStringKey("\$branch", branchName)
        + " (${getType().getName()})"
    )

    @Composable
    override fun getPreviewContent(): String =
        stringResource(Res.string.`source_configuration_preview_content_$name_$repositoryUrl_$branch`)
            .replaceStringKey("\$name", name)
            .replaceStringKey("\$repositoryUrl", repositoryUrl)
            .replaceStringKey("\$branch", branchName)

    private fun String.replaceStringKey(oldValue: String, newValue: String): String =
        replace(oldValue, newValue.ifBlank { "?" })

    @Composable
    override fun getInvalidReasonMessages(): Map<Int, String> = buildMap {
        if (repositoryUrl.isBlank()) {
            put(1, stringResource(Res.string.source_configuration_repository_url_not_set))
        }
        else if (!repositoryUrl.startsWith("http://") && !repositoryUrl.startsWith("https://")) {
            put(1, stringResource(Res.string.source_configuration_repository_url_not_http))
        }

        if (branchName.isBlank()) {
            put(2, stringResource(Res.string.source_configuration_branch_not_set))
        }
    }
}
