import util.configureAllKmpTargets

plugins {
    id("kmp-conventions")
    id("android-library-conventions")
//    id("publishing-conventions")

    alias(libs.plugins.kotlin)
    alias(libs.plugins.publish)
//    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    configureAllKmpTargets()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.specification)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(projects.core.accessor)
                implementation(projects.core.test)
            }
        }
    }
}

val projectName: String = libs.versions.project.name.get()
val projectVersion: String = libs.versions.project.version.name.get()

mavenPublishing {
    coordinates("dev.toastbits.$projectName.extension", "media", projectVersion)
}
