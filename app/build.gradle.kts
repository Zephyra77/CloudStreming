import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("android") // versi Kotlin ikut Android Gradle Plugin
    id("com.android.application")
    id("org.jetbrains.dokka") version "1.9.20"
}

val javaTarget = "17"

fun getGitCommitHash(): String {
    return try {
        val headFile = file("${project.rootDir}/.git/HEAD")
        if (headFile.exists()) {
            val headContent = headFile.readText().trim()
            if (headContent.startsWith("ref:")) {
                val refPath = headContent.substring(5)
                val commitFile = file("${project.rootDir}/.git/$refPath")
                if (commitFile.exists()) commitFile.readText().trim() else ""
            } else {
                headContent
            }
        } else {
            ""
        }
    } catch (_: Throwable) {
        ""
    }.take(7)
}

android {
    namespace = "com.lagradost.cloudstream3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lagradost.cloudstream3"
        minSdk = 21
        targetSdk = 34
        versionCode = 67
        versionName = "4.6.0"

        resValue("string", "app_version", "${versionName}")
        resValue("string", "commit_hash", getGitCommitHash())
        resValue("bool", "is_prerelease", "false")
    }

    signingConfigs {
        val tmpFilePath = System.getProperty("user.home") + "/work/_temp/keystore/"
        val prereleaseStoreFile: File? = File(tmpFilePath).listFiles()?.first()
        if (prereleaseStoreFile != null) {
            create("prerelease") {
                storeFile = prereleaseStoreFile
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
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
            if (signingConfigs.names.contains("prerelease")) {
                signingConfig = signingConfigs.getByName("prerelease")
            } else {
                logger.warn("No prerelease signing config!")
            }
            versionNameSuffix = "-PRE"
            versionCode = (System.currentTimeMillis() / 60000).toInt()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = javaTarget
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xjvm-default=all-compatibility",
            "-Xannotation-default-target=param-property",
            "-opt-in=com.lagradost.cloudstream3.Prerelease"
        )
    }

    buildFeatures {
        buildConfig = true
        viewBinding {
            enable = true
        }
    }

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
    implementation(kotlin("stdlib"))

    // Android Core & Lifecycle
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Media 3 & Video
    implementation("androidx.media3:media3-exoplayer:1.1.0")
    implementation("androidx.media3:media3-ui:1.1.0")

    // Networking & JSON
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")

    // Desugar
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
}

tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
    moduleName.set("Cloudstream App")
}
