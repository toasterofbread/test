import util.configureAllKmpTargets

plugins {
    id("kmp-conventions")
    id("android-library-conventions")

    alias(libs.plugins.kotlin)
    alias(libs.plugins.publish)
}

kotlin {
    configureAllKmpTargets()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core.specification)

                api(libs.kogit.core)
                api(libs.kotlinx.coroutines.core)
                api(libs.okio)
                implementation(libs.uri.kmp)
            }
        }

        val jvmAndNativeMain by getting {
            dependencies {
                implementation(libs.kogit.system)
            }
        }

        val jvmAndNativeTest by getting {
            dependencies {
                implementation(projects.core.test)
                // temp
                implementation(projects.extension.mediawatch)
            }
        }
    }
}
