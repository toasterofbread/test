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
                api(libs.kotlinx.datetime)
                api(libs.okio)
                implementation(libs.markdown)
                implementation(libs.uri.kmp)
//                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

val projectName: String = libs.versions.project.name.get()
val projectVersion: String = libs.versions.project.version.name.get()

mavenPublishing {
    coordinates("dev.toastbits.$projectName", "core.specification", projectVersion)
}
