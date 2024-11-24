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
                implementation(projects.application.dbsource.domain)
                implementation(projects.application.worker)
                implementation(projects.core.specification)
                implementation(projects.core.accessor)

                implementation(libs.kogit.core)
                implementation(libs.composekit.settings)
                implementation(libs.ktor.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
