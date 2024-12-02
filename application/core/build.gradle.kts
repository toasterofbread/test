import util.configureAllComposeTargets

plugins {
    id("android-library-conventions")
    id("compose-conventions")
    alias(libs.plugins.kotlin)
}

kotlin {
    configureAllComposeTargets()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.specification)

                implementation(libs.composekit.util)
                implementation(libs.composekit.navigation)
                implementation(libs.composekit.components)
                implementation(libs.composekit.theme)
            }
        }
    }
}
