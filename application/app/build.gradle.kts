import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import util.configureAllComposeTargets

plugins {
    id("android-application-conventions")
    id("compose-conventions")

    alias(libs.plugins.kotlin)
}

kotlin {
    configureAllComposeTargets {
        when (this) {
            is KotlinWasmJsTargetDsl -> {
                browser {
                    commonWebpackConfig {
                        outputFileName = "client.js"
                        devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                            static = (static ?: mutableListOf()).apply {
                                // Serve sources to debug inside browser
                                add(project.projectDir.path)
                                add(project.projectDir.path + "/commonMain/")
                                add(project.projectDir.path + "/wasmJsMain/")
                            }
                        }
                    }
                }
                binaries.executable()
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.application.core)
                implementation(projects.application.dbsource.data)
                implementation(projects.application.settings.data)
                implementation(projects.application.logview.data)
                implementation(projects.application.worker)

                implementation(projects.extension.media)
                implementation(projects.extension.mediawatch)
                implementation(projects.extension.gdocs)

                implementation(libs.composekit)

                implementation(libs.okio)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        buildTypes.release {
            proguard {
                // TODO
                isEnabled = false
            }
        }
    }
}

tasks.withType<AbstractCopyTask> {
    exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
}

tasks.named {
    it == "wasmJsBrowserDevelopmentRun" || it == "wasmJsBrowserProductionRun" || it == "wasmJsBrowserRun"
}.all {
    doFirst {
        throw GradleException("Browser run tasks are not supported. Use a distribution task and run a server manually.")
    }
}

tasks.named("wasmJsBrowserDistribution") {
    dependOnTaskAndCopyOutputDirectory(":application:worker:wasmJsBrowserDistribution", "productionExecutable")
    printOutputsOnCompletion()
}

tasks.named("wasmJsBrowserDevelopmentExecutableDistribution") {
    dependOnTaskAndCopyOutputDirectory(":application:worker:wasmJsBrowserDevelopmentExecutableDistribution", "developmentExecutable")
    printOutputsOnCompletion()
}

fun Task.dependOnTaskAndCopyOutputDirectory(taskPath: String, dirName: String) {
    dependsOn(taskPath)

    outputs.upToDateWhen { false }

    doLast {
        val appProductionExecutable: File = outputs.files.single { it.name == dirName }

        val taskParts: List<String> = taskPath.split(':').filter { it.isNotBlank() }
        var currentProject = rootProject
        for (i in 0 until taskParts.size - 1) {
            currentProject = currentProject.project(taskParts[i])
        }

        val workerBuildTask: Task by currentProject.tasks.named(taskParts.last())
        val workerProductionExecutable: File = workerBuildTask.outputs.files.single { it.name == dirName }

        for (file in workerProductionExecutable.listFiles().orEmpty()) {
            file.copyRecursively(appProductionExecutable.resolve(file.name), overwrite = true)
        }
    }
}

fun Task.printOutputsOnCompletion() {
    doLast {
        val outputs: List<String> = outputs.files.map { it.absolutePath }
        println("\nTask outputs: $outputs")
    }
}
