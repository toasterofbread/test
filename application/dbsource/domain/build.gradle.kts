import util.configureAllComposeTargets

plugins {
    id("android-library-conventions")
    id("compose-conventions")
}

kotlin {
    configureAllComposeTargets()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.application.worker)
                implementation(projects.core.specification)
                implementation(projects.core.accessor)

                implementation(libs.kogit.core)
                implementation(libs.composekit.util)
                implementation(libs.composekit.settings)
                implementation(libs.ktor.core)
            }
        }
    }
}
