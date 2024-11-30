package dev.toastbits.lifelog.application.dbsource.inmemorygit.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import okio.Path.Companion.toPath
import kotlin.test.Test

class GitRepositoryFileUrlProviderTest {
    @Test
    fun getGitRepositoryFileUrl_githubNoLine_correctUrl() {
        val url: String? =
            GitRepositoryFileUrlProvider.getGitRepositoryFileUrl(
                repositoryUrl = "https://github.com/torvalds/linux",
                revision = "master",
                filePath = "LICENSES/preferred/GPL-2.0".toPath(),
                lineIndex = null
            )

        assertThat(url).isEqualTo("https://github.com/torvalds/linux/blob/master/LICENSES/preferred/GPL-2.0?plain=1")
    }

    @Test
    fun getGitRepositoryFileUrl_githubWithLine_correctUrl() {
        val url: String? =
            GitRepositoryFileUrlProvider.getGitRepositoryFileUrl(
                repositoryUrl = "https://github.com/torvalds/linux",
                revision = "master",
                filePath = "LICENSES/preferred/GPL-2.0".toPath(),
                lineIndex = 19U
            )

        assertThat(url).isEqualTo("https://github.com/torvalds/linux/blob/master/LICENSES/preferred/GPL-2.0?plain=1#L20")
    }

    @Test
    fun getGitRepositoryFileUrl_gitlabNoLine_correctUrl() {
        val url: String? =
            GitRepositoryFileUrlProvider.getGitRepositoryFileUrl(
                repositoryUrl = "https://gitlab.com/gitlab-org/gitlab",
                revision = "master",
                filePath = "LICENSE".toPath(),
                lineIndex = null
            )

        assertThat(url).isEqualTo("https://gitlab.com/gitlab-org/gitlab/-/blob/master/LICENSE")
    }

    @Test
    fun getGitRepositoryFileUrl_gitlabWithLine_correctUrl() {
        val url: String? =
            GitRepositoryFileUrlProvider.getGitRepositoryFileUrl(
                repositoryUrl = "https://gitlab.com/gitlab-org/gitlab",
                revision = "master",
                filePath = "LICENSE".toPath(),
                lineIndex = 2U
            )

        assertThat(url).isEqualTo("https://gitlab.com/gitlab-org/gitlab/-/blob/master/LICENSE#L3")
    }

    @Test
    fun getGitRepositoryFileUrl_otherHost_nullUrl() {
        val url: String? =
            GitRepositoryFileUrlProvider.getGitRepositoryFileUrl(
                repositoryUrl = "https://google.com/gitlab-org/gitlab",
                revision = "master",
                filePath = "LICENSE".toPath(),
                lineIndex = null
            )

        assertThat(url).isNull()
    }
}
