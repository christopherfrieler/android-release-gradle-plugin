import org.gradle.jvm.tasks.Jar

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.4.31"
    id("org.jetbrains.dokka") version "1.4.20"
}

repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
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

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
}

tasks {
    test {
        useJUnitPlatform()
    }

    register("kdocJar", Jar::class) {
        dependsOn(dokkaHtml)
        from("${buildDir}/dokka")
        archiveClassifier.set("kdoc")
    }
}
