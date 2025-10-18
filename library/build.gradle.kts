import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    id("com.codingfeline.buildkonfig")
    id("org.jetbrains.dokka") version "1.9.20"
}

val javaTarget = JavaVersion.VERSION_17

kotlin {
    androidTarget()
    jvm()

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
    kotlinOptions.jvmTarget = "17"
}

buildkonfig {
    packageName = "com.lagradost.api"
    exposeObjectWithName = "BuildConfig"

    defaultConfigs {
        val isDebug = kotlin.runCatching { extra.get("isDebug") }.getOrNull() == true
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", isDebug.toString())

        val localProperties = gradleLocalProperties(rootDir)

        buildConfigField(
            FieldSpec.Type.STRING,
            "MDL_API_KEY",
            System.getenv("MDL_API_KEY") ?: localProperties["mdl.key"].toString()
        )
    }
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.lagradost.api"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = javaTarget
        targetCompatibility = javaTarget
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            groupId = "com.lagradost.api"
        }
    }
}

tasks.register<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml") {
    outputDirectory.set(buildDir.resolve("dokka"))
    dokkaSourceSets {
        named("commonMain") {
            displayName.set("Common")
            includes.from("Module.md")
        }
    }
}
