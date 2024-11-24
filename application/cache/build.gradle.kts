import util.KmpTarget
import util.configureKmpTargets

plugins {
    id("kmp-conventions")
    id("android-library-conventions")

    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    configureKmpTargets(*KmpTarget.ALL_COMPOSE)

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kogit.core)

                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
