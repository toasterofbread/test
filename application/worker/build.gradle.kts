import de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl
import util.configureAllComposeTargets
import util.library
import util.libs
import util.version

plugins {
    id("android-library-conventions")
    id("compose-conventions")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.undercouch.download)
}

kotlin {
    configureAllComposeTargets {
        when (this) {
            is KotlinWasmJsTargetDsl -> {
                browser {
                    commonWebpackConfig {
                        outputFileName = "worker.js"
                    }
                }
                binaries.executable()
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core.git.memory)

                implementation(libs.library("kotlinx.coroutines.core"))
                implementation(libs.library("kotlinx.serialization.json"))
                implementation(libs.library("ktor.core"))
                implementation(libs.library("sqldelight.extension.coroutines"))
                implementation(libs.library("composekit.context"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.library("sqldelight.driver.jvm"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.library("sqldelight.driver.android"))
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.library("sqldelight.driver.wasmjs"))
            }

            resources.srcDir(layout.buildDirectory.dir("sqlite"))
        }
    }
}

sqldelight {
    databases {
        create("GitDatabase") {
            packageName.set("dev.toastbits.lifelog.application.worker")
            generateAsync.set(true)
        }
    }
}

val sqliteYear: Int = libs.version("sqlite.wasm.year").toInt()
val sqliteVersion: Int = libs.version("sqlite.wasm.version").toInt()

val sqliteDownloadTask: Download by
    tasks.register("sqliteDownload", Download::class.java) {
        src("https://sqlite.org/$sqliteYear/sqlite-wasm-$sqliteVersion.zip")
        dest(layout.buildDirectory.dir("sqlite-zip"))
        onlyIfModified(true)
    }

val sqliteUnzipTask: Copy by
    tasks.register("sqliteUnzip", Copy::class.java) {
        dependsOn(sqliteDownloadTask)
        from(zipTree(layout.buildDirectory.dir("sqlite-zip/sqlite-wasm-$sqliteVersion.zip"))) {
            include("sqlite-wasm-$sqliteVersion/jswasm/**")
            exclude("**/*worker1*")

            eachFile {
                relativePath = RelativePath(true, *relativePath.segments.drop(2).toTypedArray())
            }
        }
        into(layout.buildDirectory.dir("sqlite"))
        includeEmptyDirs = false
    }

tasks.named("wasmJsProcessResources") {
    dependsOn(sqliteUnzipTask)
}

tasks.named("wasmJsBrowserDevelopmentWebpack") {
    patchWorkerJsFile("developmentExecutable", "__webpack_require__.b = document.baseURI || self.location.href;")
}

tasks.named("wasmJsBrowserProductionWebpack") {
    patchWorkerJsFile("productionExecutable", ",i.b=document.baseURI||self.location.href")
}

fun Task.patchWorkerJsFile(
    outputDirectoryName: String,
    vararg textToRemove: String
) {
    val outputDirectory: File = outputs.files.first { it.name == outputDirectoryName }

    doLast {
        val workerFile: File = outputDirectory.resolve("worker.js")
        check(workerFile.isFile) { "patchWorkerJsFile failed. No file found at ${workerFile.absolutePath}" }

        val lines: MutableList<String> = workerFile.readLines().toMutableList()

        val toRemove: MutableList<String> = textToRemove.toMutableList()

        val i: MutableListIterator<String> = lines.listIterator()
        while (i.hasNext()) {
            var line: String = i.next()

            val r: MutableIterator<String> = toRemove.iterator()
            while (r.hasNext()) {
                val text: String = r.next()
                val start: Int = line.indexOf(text)
                if (start != -1) {
                    line = line.removeRange(start until start + text.length)
                    r.remove()
                }
            }

            i.set(line)

            if (toRemove.isEmpty()) {
                break
            }
        }

        check(toRemove.isEmpty()) { "patchWorkerJsFile failed. Could not remove ${toRemove.toList()} from ${workerFile.absolutePath}" }

        workerFile.writeText(lines.joinToString("\n"))
    }
}
