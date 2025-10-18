import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("android")
    id("com.android.application")
    id("org.jetbrains.dokka") version "1.9.20"
}

val javaTarget = "17"

android {
    namespace = "com.lagradost.cloudstream3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lagradost.cloudstream3"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = javaTarget
    }

    viewBinding { enable = true }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(project(":library"))

    // Kotlin + Core
    implementation(kotlin("stdlib"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Media 3 (ExoPlayer)
    implementation(libs.bundles.media3)
    implementation(libs.video)

    // Playback & Utilities
    implementation(libs.colorpicker)
    implementation(libs.newpipeextractor)
    implementation(libs.juniversalchardet)

    // FFmpeg Decoding
    implementation(libs.bundles.nextlibMedia3)

    // Crash Reporting
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
    implementation(libs.conscrypt.android) { version { strictly("2.5.2") } }
    implementation(libs.jackson.module.kotlin) { version { strictly("2.13.1") } }

    // Torrent Support
    implementation(libs.torrentserver)

    // Downloading & Networking
    implementation(libs.work.runtime)
    implementation(libs.work.runtime.ktx)
    implementation(libs.nicehttp)
}

tasks.withType<KotlinJvmCompile> {
    compilerOptions {
        jvmTarget.set(javaTarget)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all-compatibility",
            "-Xannotation-default-target=param-property",
            "-opt-in=com.lagradost.cloudstream3.Prerelease"
        )
    }
}

// Dokka configuration: gunakan task yang sudah ada
tasks.named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml") {
    outputDirectory.set(buildDir.resolve("dokka"))
    moduleName.set("Cloudstream App")
}
