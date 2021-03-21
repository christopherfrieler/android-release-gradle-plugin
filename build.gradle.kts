import org.gradle.jvm.tasks.Jar

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.4.31"
    id("org.jetbrains.dokka") version "1.4.20"
    id("maven-publish")
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
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
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

publishing {
    publications {
        afterEvaluate {
            getByName("pluginMaven", MavenPublication::class) {
                pom {
                    name.set("android-release-gradle-plugin")
                    description.set("A Gradle plugin to automate scm-releases of android apps.")
                    url.set("https://github.com/christopherfrieler/android-release-gradle-plugin")
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    scm { url.set("https://github.com/christopherfrieler/android-release-gradle-plugin") }
                    developers {
                        developer { name.set("Christopher Frieler") }
                    }
                }

                artifact(tasks.kotlinSourcesJar)
                artifact(tasks.getByName("kdocJar"))
            }
        }
    }
}
