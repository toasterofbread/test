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
                api(projects.application.settings.domain)
                implementation(projects.core.specification)
                implementation(projects.application.dbsource.domain)
                implementation(projects.application.dbsource.inmemorygit)

                implementation(libs.kogit.system)
                implementation(libs.composekit.navigation)
                implementation(libs.composekit.components)
                implementation(libs.composekit.settingsitem)
            }
        }
    }
}
