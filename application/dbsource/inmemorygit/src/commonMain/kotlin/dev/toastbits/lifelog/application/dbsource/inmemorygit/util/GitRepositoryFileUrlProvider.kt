package dev.toastbits.lifelog.application.dbsource.inmemorygit.util

import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import okio.Path

object GitRepositoryFileUrlProvider {
    fun getGitRepositoryFileUrl(
        repositoryUrl: String,
        revision: String,
        filePath: Path,
        lineIndex: UInt?
    ): String? =
        URLBuilder(repositoryUrl).apply {
            if (host == "github.com") {
                appendPathSegments("blob", revision)
                appendPathSegments(filePath.segments)
                parameters["plain"]= "1"
            }
            else if (host == "gitlab.com" || host.endsWith(".gitlab.com")) {
                appendPathSegments("-", "blob", revision)
                appendPathSegments(filePath.segments)
            }
            else {
                return null
            }
        }.buildString().let { url ->
            if (lineIndex != null) url + "#L${lineIndex + 1U}"
            else url
        }
}
