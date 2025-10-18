import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.dokka") version "1.9.20"
}

val javaTarget = JavaVersion.VERSION_17
val tmpFilePath = System.getProperty("user.home") + "/work/_temp/keystore/"
val prereleaseStoreFile: File? = File(tmpFilePath).listFiles()?.first()

fun getGitCommitHash(): String {
    return try {
        val headFile = file("${project.rootDir}/.git/HEAD")
        if (headFile.exists()) {
            val headContent = headFile.readText().trim()
            if (headContent.startsWith("ref:")) {
                val refPath = headContent.substring(5)
                val commitFile = file("${project.rootDir}/.git/$refPath")
                if (commitFile.exists()) commitFile.readText().trim() else ""
            } else headContent
        } else ""
    } catch (_: Throwable) {
        ""
    }.take(7)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.lagradost.cloudstream3"

    defaultConfig {
        applicationId = "com.lagradost.cloudstream3"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 67
        versionName = "4.6.0"

        resValue("string", "app_version", "${versionName}")
        resValue("string", "commit_hash", getGitCommitHash())
        resValue("bool", "is_prerelease", "false")

        val localProperties = gradleLocalProperties(rootDir)
        buildConfigField(
            "String",
            "SIMKL_CLIENT_ID",
            "\"" + (System.getenv("SIMKL_CLIENT_ID") ?: localProperties["simkl.id"]) + "\""
        )
        buildConfigField(
            "String",
            "SIMKL_CLIENT_SECRET",
            "\"" + (System.getenv("SIMKL_CLIENT_SECRET") ?: localProperties["simkl.secret"]) + "\""
        )
    }

    signingConfigs {
        prereleaseStoreFile?.let {
            create("prerelease") {
                storeFile = it
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    flavorDimensions.add("state")
    productFlavors {
        create("stable") {
            dimension = "state"
            resValue("bool", "is_prerelease", "false")
        }
        create("prerelease") {
            dimension = "state"
            resValue("bool", "is_prerelease", "true")
            buildConfigField("boolean", "BETA", "true")
            applicationIdSuffix = ".prerelease"
            signingConfig = signingConfigs.findByName("prerelease")
            versionNameSuffix = "-PRE"
            versionCode = (System.currentTimeMillis() / 60000).toInt()
        }
    }

    compileOptions {
        sourceCompatibility = javaTarget
        targetCompatibility = javaTarget
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(project(":library"))

    // Core
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    // Media 3 / Video
    implementation(libs.bundles.media3)
    implementation(libs.video)
    implementation(libs.bundles.nextlibMedia3)

    // Playback & Utilities
    implementation(libs.colorpicker)
    implementation(libs.newpipeextractor)
    implementation(libs.juniversalchardet)

    // Crash Reporting
    implementation(libs.acra.core)
    implementation(libs.acra.toast)

    // UI
    implementation(libs.shimmer)
    implementation(libs.palette.ktx)
    implementation(libs.tvprovider)
    implementation(libs.overlappingpanels)
    implementation(libs.biometric)
    implementation(libs.previewseekbar.media3)
    implementation(libs.qrcode.kotlin)

    // Extensions / JS / Parsing
    implementation(libs.rhino)
    implementation(libs.quickjs)
    implementation(libs.fuzzywuzzy)
    implementation(libs.safefile)

    // Networking & Download
    implementation(libs.work.runtime)
    implementation(libs.work.runtime.ktx)
    implementation(libs.nicehttp)

    // Security / Compatibility
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)
    implementation(libs.conscrypt.android) {
        version { strictly("2.5.2") }
    }
    implementation(libs.jackson.module.kotlin) {
        version { strictly("2.13.1") }
    }

    // Torrent Support
    implementation(libs.torrentserver)
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xjvm-default=all-compatibility",
        "-Xannotation-default-target=param-property",
        "-opt-in=com.lagradost.cloudstream3.Prerelease"
    )
}

tasks.register<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml") {
    outputDirectory.set(buildDir.resolve("dokka"))
    moduleName.set("Cloudstream App")
}
