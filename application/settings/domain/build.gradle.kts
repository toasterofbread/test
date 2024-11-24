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
                api(projects.core.specification)
                api(projects.core.accessor)
                api(projects.core.git.core)

                api(libs.composekit.commonsettings)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
