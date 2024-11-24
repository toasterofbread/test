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
                 implementation(libs.composekit.navigation)
            }
        }
    }
}
