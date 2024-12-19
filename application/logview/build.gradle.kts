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
                implementation(projects.application.core)
                implementation(projects.application.usercontent)
                implementation(projects.application.settings.data)
                implementation(projects.application.dbsource.domain)
                implementation(projects.core.specification)

                implementation(libs.composekit.theme.core)
                implementation(libs.composekit.components)
                implementation(libs.composekit.util)
                implementation(libs.composekit.navigation)

                implementation(libs.diff)
            }
        }
    }
}
