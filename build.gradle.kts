import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.dokka") version "2.0.0"
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

repositories {
    mavenCentral()
}

group = "rocks.frieler.android"
version = "0.6.0-SNAPSHOT"

gradlePlugin {
    plugins {
        create(project.name) {
            id = "rocks.frieler.android.release"
            implementationClass = "rocks.frieler.android.release.gradle.AndroidReleasePlugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks {
    test {
        useJUnitPlatform()
    }

    register("javadocJar", Jar::class) {
        dependsOn(dokkaGenerate)
        from("${layout.buildDirectory}/dokka/javadoc")
        archiveClassifier.set("javadoc")
    }
}

publishing {
    publications {
        fun MavenPom.addCommonPublicationSettings() {
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

        afterEvaluate {
            getByName("pluginMaven", MavenPublication::class) {
                pom {
                    name.set(project.name)
                    description.set("A Gradle plugin to automate scm-releases of android apps.")
                    addCommonPublicationSettings()
                }

                artifact(tasks.kotlinSourcesJar)
                artifact(tasks.getByName("javadocJar"))
            }
            getByName("${project.name}PluginMarkerMaven", MavenPublication::class) {
                pom {
                    name.set("${project.name}-marker")
                    description.set("Gradle plugin marker for ${project.name}")
                    addCommonPublicationSettings()
                }
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
    System.getenv("SIGNING_KEY_ID")?.also { signingKeyId ->
        sign(publishing.publications)
        project.setProperty("signing.keyId", signingKeyId)
        project.setProperty("signing.secretKeyRingFile", rootProject.file("$signingKeyId.gpg"))
        project.setProperty("signing.password", System.getenv("SIGNING_KEY_PASSWORD"))
    }
}

nexusPublishing {
    packageGroup = project.group as String
    this.repositories {
        sonatype {
            stagingProfileId = System.getenv("SONATYPE_STAGING_PROFILE_ID")
            username = System.getenv("SONATYPE_USERNAME")
            password = System.getenv("SONATYPE_PASSWORD")
        }
    }
}
