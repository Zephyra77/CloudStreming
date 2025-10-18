import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaSourceSetBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("com.android.library")
    id("com.codingfeline.buildkonfig") version "0.15.1"
    id("org.jetbrains.dokka") version "1.9.20"
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

val javaTarget = JvmTarget.fromTarget("17")

kotlin {
    androidTarget()
    jvm()

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
            "-Xannotation-default-target=param-property"
        )
    }

    sourceSets {
        all {
            languageSettings.optIn("com.lagradost.cloudstream3.Prerelease")
        }

        commonMain.dependencies {
            implementation(libs.nicehttp)
            implementation(libs.jackson.module.kotlin)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.fuzzywuzzy)
            implementation(libs.rhino)
            implementation(libs.newpipeextractor)
            implementation(libs.tmdb.java)
        }
    }
}

tasks.withType<KotlinJvmCompile> {
    compilerOptions.jvmTarget.set(javaTarget)
}

buildkonfig {
    packageName = "com.lagradost.api"
    exposeObjectWithName = "BuildConfig"

    defaultConfigs {
        val isDebug = kotlin.runCatching { extra.get("isDebug") }.getOrNull() == true
        if (isDebug) {
            logger.quiet("Compiling library with debug flag")
        } else {
            logger.quiet("Compiling library with release flag")
        }

        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", isDebug.toString())

        // ✅ hanya 1 argumen sekarang (Gradle 8+)
        val localProperties = gradleLocalProperties(rootDir)

        buildConfigField(
            FieldSpec.Type.STRING,
            "MDL_API_KEY", (System.getenv("MDL_API_KEY") ?: localProperties["mdl.key"]).toString()
        )
    }
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    namespace = "com.lagradost.api"

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaTarget.target)
        targetCompatibility = JavaVersion.toVersion(javaTarget.target)
    }

    // ❌ targetSdk di lint/testOptions dihapus, udah deprecated
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            groupId = "com.lagradost.api"
        }
    }
}

tasks.register<DokkaTask>("dokkaHtml") {
    outputDirectory.set(buildDir.resolve("dokka"))
    moduleName.set("Library")

    dokkaSourceSets.configureEach {
        displayName.set("Android/JVM")
        skipEmptyPackages.set(true)

        includes.from("README.md")

        sourceLink {
            localDirectory.set(file(".."))
            remoteUrl.set(
                uri("https://github.com/recloudstream/cloudstream/tree/master").toURL()
            )
            remoteLineSuffix.set("#L")
        }
    }
}
