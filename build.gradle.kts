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
            storeFile = file("keystore.jks") // letakkan keystore sendiri
            storePassword = "YOUR_STORE_PASSWORD"
            keyAlias = "YOUR_KEY_ALIAS"
            keyPassword = "YOUR_KEY_PASSWORD"
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
