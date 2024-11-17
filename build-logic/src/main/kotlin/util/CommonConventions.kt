package util

import org.gradle.api.Project
import org.gradle.api.file.Directory

object CommonConventions {
    fun getOutputDirectory(project: Project): Directory =
        project.layout.buildDirectory.dir("outputs").get()

    fun getBaseOutputFileName(
        project: Project,
        platform: OutputPlatform,
        isDebug: Boolean?
    ): String = with (project) {
        buildString {
            append(rootProject.name.lowercase() + "-" + libs.version("project-version-name") + "-" + platform.displayName)
            if (isDebug == true) {
                append("-debug")
            }
        }
    }

    enum class OutputPlatform(val displayName: String) {
        ANDROID_UNIVERSAL("android-universal"),
        LINUX_X86_64("linux-x86_64"),
        WASM("web-wasm");

        fun getBaseOutputFileName(
            project: Project,
            isDebug: Boolean?
        ): String =
            getBaseOutputFileName(project, this, isDebug)
    }
}
