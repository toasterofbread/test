plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()

    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(libs.kotlin.plugin)
    implementation(libs.agp)
    implementation(libs.kotlin.compose.plugin)
    implementation(libs.compose.plugin)
    implementation(libs.vanniktech.publish.plugin)
    implementation(libs.mokkery.plugin)
}
