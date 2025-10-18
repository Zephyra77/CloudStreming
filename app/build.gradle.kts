import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("com.android.application")
    id("kotlin-android")
}

val javaTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
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
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    viewBinding {
        enable = true
    }

    signingConfigs {
        if (prereleaseStoreFile != null) {
            create("prerelease") {
                storeFile = file(prereleaseStoreFile)
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }
    }

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.lagradost.cloudstream3"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 67
        versionName = "4.6.0"

        resValue("string", "app_version", "${defaultConfig.versionName}${versionNameSuffix ?: ""}")
        resValue("string", "commit_hash", getGitCommitHash())
        resValue("bool", "is_prerelease", "false")

        val localProperties = gradleLocalProperties(rootDir)

        buildConfigField(
            "long",
            "BUILD_DATE",
            "${System.currentTimeMillis()}"
        )
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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.toVersion(javaTarget.target)
        targetCompatibility = JavaVersion.toVersion(javaTarget.target)
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    buildFeatures {
        buildConfig = true
    }

    namespace = "com.lagradost.cloudstream3"
}

dependencies {
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.json)
    androidTestImplementation(libs.core)
    implementation(libs.junit.ktx)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Android Core & Lifecycle
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.bundles.navigationKtx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Design & UI
    implementation(libs.preference.ktx)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)

    // Coil Image Loading
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)

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
        version {
            strictly("2.5.2")
        }
    }
    implementation(libs.jackson.module.kotlin) {
        version {
            strictly("2.13.1")
        }
    }

    // Torrent Support
    implementation(libs.torrentserver)

    // Downloading & Networking
    implementation(libs.work.runtime)
    implementation(libs.work.runtime.ktx)
    implementation(libs.nicehttp)

    implementation(project(":library") {
        val isDebug = gradle.startParameter.taskRequests.any { task ->
            task.args.any { arg -> arg.contains("debug", true) }
        }
        this.extra.set("isDebug", isDebug)
    })
}

tasks.register<Jar>("androidSourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

tasks.register<Copy>("copyJar") {
    from(
        "build/intermediates/compile_app_classes_jar/prereleaseDebug/bundlePrereleaseDebugClassesToCompileJar",
        "../library/build/libs"
    )
    into("build/app-classes")
    include("classes.jar", "library-jvm*.jar")
    rename("library-jvm.*.jar", "library-jvm.jar")
}

tasks.register<Jar>("makeJar") {
    duplicatesStrategy = DuplicatesStrategy.FAIL
    dependsOn(tasks.getByName("copyJar"))
    from(
        zipTree("build/app-classes/classes.jar"),
        zipTree("build/app-classes/library-jvm.jar")
    )
    destinationDirectory.set(layout.buildDirectory)
    archiveBaseName = "classes"
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
