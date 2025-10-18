plugins {
    id("com.android.application") version "8.1.1"
    kotlin("android") version "1.9.0"
}

android {
    namespace = "com.lagradost.cloudstream3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lagradost.cloudstream3"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("release") {
            // Keystore akan di-inject lewat GitHub Actions
            storeFile = file("keystore.jks")
            storePassword = project.findProperty("android.injected.signing.store.password")?.toString() ?: ""
            keyAlias = project.findProperty("android.injected.signing.key.alias")?.toString() ?: ""
            keyPassword = project.findProperty("android.injected.signing.key.password")?.toString() ?: ""
        }
        getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        create("prerelease") {
            initWith(getByName("release"))
            versionNameSuffix = "-prerelease"
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
    google()
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":library"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
