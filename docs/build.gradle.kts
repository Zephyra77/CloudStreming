plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka") version "1.9.20"
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

tasks.dokkaHtml {
    moduleName.set("Cloudstream")

    dokkaSourceSets {
        named("main") {
            includes.from("README.md")
            sourceLink {
                localDirectory.set(file("../"))
                remoteUrl.set(uri("https://github.com/recloudstream/cloudstream/tree/master").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
}
