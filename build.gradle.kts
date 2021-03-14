plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.4.31"
}

repositories {
    mavenCentral()
}

group = "rocks.frieler.android"
version = "0.1.0-SNAPSHOT"

gradlePlugin {
    plugins {
        create("android-release-gradle-plugin") {
            id = "rocks.frieler.android.release"
            implementationClass = "rocks.frieler.android.release.gradle.AndroidReleasePlugin"
        }
    }
}
