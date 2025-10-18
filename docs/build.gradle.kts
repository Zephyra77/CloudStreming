plugins {
    kotlin("jvm") version "1.9.24"
    id("org.jetbrains.dokka") version "1.9.20"
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    dokka(project(":app"))
    dokka(project(":library"))
}

dokka {
    moduleName.set("Cloudstream")
}
