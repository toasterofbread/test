import util.configureAllComposeTargets

plugins {
    id("android-library-conventions")
    id("compose-conventions")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    configureAllComposeTargets()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.application.dbsource.domain)
                implementation(projects.application.core)
                implementation(projects.application.settings.domain)
                implementation(projects.application.settings.data)
                implementation(projects.application.logview)
                implementation(projects.core.specification)

                implementation(libs.kogit.core)
                implementation(libs.composekit.theme.core)
                implementation(libs.composekit.util)
                implementation(libs.composekit.navigation)
                implementation(libs.composekit.components)
                implementation(libs.ktor.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
