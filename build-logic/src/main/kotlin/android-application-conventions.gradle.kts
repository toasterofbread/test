import com.android.build.api.dsl.ApkSigningConfig
import util.CommonConventions
import util.configureAndroid
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
}

fun NamedDomainObjectContainer<out ApkSigningConfig>.loadSigningConfig(name: String, filePath: String) {
    val file: File = project.file(filePath)
    if (!file.isFile) {
        return
    }

    val keystore: Properties =
        Properties().apply {
            load(FileInputStream(file))
        }

    create(name) {
        storeFile = file(keystore["storeFile"] as String)
        storePassword = keystore["storePassword"] as String
        keyAlias = keystore["keyAlias"] as String
        keyPassword = keystore["keyPassword"] as String
    }
}

android {
    configureAndroid(project)

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        loadSigningConfig("release", "release-keystore.properties")
    }

    buildTypes {
        all {
            setProperty(
                "archivesBaseName",
                CommonConventions.OutputPlatform.ANDROID_UNIVERSAL.getBaseOutputFileName(project, null)
            )
        }

        getByName("release") {
            isMinifyEnabled = true

            try {
                signingConfig = signingConfigs.getByName("release")
            }
            catch (_: UnknownDomainObjectException) {}
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"

            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
