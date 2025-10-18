plugins {
    kotlin("android") version "1.9.24"
    id("com.android.library")
    id("org.jetbrains.dokka") version "1.9.20"
    id("com.codingfeline.buildkonfig") version "0.15.1"
}

android {
    namespace = "com.lagradost.cloudstream3"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

buildkonfig {
    packageName = "com.lagradost.cloudstream3"
    objectName = "BuildConfig"
    defaultConfigs {
        buildConfigField(STRING, "APP_NAME", "Cloudstream")
        buildConfigField(STRING, "APP_VERSION", "1.0.0")
    }
}

tasks.register("generateDocs") {
    dependsOn(tasks.dokkaHtml)
}

// ✅ Dokka konfigurasi yang benar
tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokka"))
    moduleName.set("Cloudstream Library")
}
