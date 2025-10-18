plugins {
    kotlin("android") version "1.9.24"
    id("com.android.application")
    id("org.jetbrains.dokka") version "1.9.20"
}

android {
    namespace = "com.cloudstream.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cloudstream.app"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xjvm-default=all-compatibility",
            "-Xannotation-default-target=param-property",
            "-opt-in=com.lagradost.cloudstream3.Prerelease"
        )
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    // 🧩 Core project dependency
    implementation(project(":library"))

    // Media 3 (ExoPlayer)
    implementation(libs.bundles.media3)
    implementation(libs.video)

    // PlayBack
    implementation(libs.colorpicker)
    implementation(libs.newpipeextractor)
    implementation(libs.juniversalchardet)

    // FFmpeg Decoding
    implementation(libs.bundles.nextlibMedia3)

    // Crash Reports
    implementation(libs.acra.core)
    implementation(libs.acra.toast)

    // UI Stuff
    implementation(libs.shimmer)
    implementation(libs.palette.ktx)
    implementation(libs.tvprovider)
    implementation(libs.overlappingpanels)
    implementation(libs.biometric)
    implementation(libs.previewseekbar.media3)
    implementation(libs.qrcode.kotlin)

    // Extensions & Other Libs
    implementation(libs.rhino)
    implementation(libs.quickjs)
    implementation(libs.fuzzywuzzy)
    implementation(libs.safefile)
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)
    implementation(libs.conscrypt.android) {
        version { strictly("2.5.2") }
    }
    implementation(libs.jackson.module.kotlin) {
        version { strictly("2.13.1") }
    }

    // Torrent Support
    implementation(libs.torrentserver)

    // Downloading & Networking
    implementation(libs.work.runtime)
    implementation(libs.work.runtime.ktx)
    implementation(libs.nicehttp)
}

// 🧱 Dokka task modern (tanpa import tambahan)
tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
    moduleName.set("Cloudstream App")
}

// 🧱 Optional: tambahkan task generateDocs
tasks.register("generateDocs") {
    dependsOn(tasks.dokkaHtml)
}
