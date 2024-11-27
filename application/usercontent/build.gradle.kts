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

                implementation(libs.composekit.theme)
                implementation(libs.composekit.util)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor)
            }
        }
    }
}
