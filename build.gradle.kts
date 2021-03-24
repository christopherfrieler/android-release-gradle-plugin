import org.gradle.jvm.tasks.Jar

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.4.31"
    id("org.jetbrains.dokka") version "1.4.20"
    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
}

group = "rocks.frieler.android"
version = "0.2.0-SNAPSHOT"

gradlePlugin {
    plugins {
        create("android-release-gradle-plugin") {
            id = "rocks.frieler.android.release"
            implementationClass = "rocks.frieler.android.release.gradle.AndroidReleasePlugin"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
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

    repositories {
        maven {
            name = "sonatype-staging"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications)

    System.getenv("SIGNING_KEY_ID")?.also { signingKeyId ->
        project.setProperty("signing.keyId", signingKeyId)
        project.setProperty("signing.secretKeyRingFile", rootProject.file("$signingKeyId.gpg"))
        project.setProperty("signing.password", System.getenv("SIGNING_KEY_PASSWORD"))
    }
}
