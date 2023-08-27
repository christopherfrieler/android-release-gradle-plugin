import org.gradle.jvm.tasks.Jar

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.dokka") version "1.8.20"
    id("maven-publish")
    id("signing")
    id("io.codearte.nexus-staging") version "0.30.0"
}

repositories {
    mavenCentral()
}

group = "rocks.frieler.android"
version = "0.4.0-SNAPSHOT"

gradlePlugin {
    plugins {
        create(project.name) {
            id = "rocks.frieler.android.release"
            implementationClass = "rocks.frieler.android.release.gradle.AndroidReleasePlugin"
        }
    }
}

tasks {
    compileJava {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }
    compileKotlin {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}

tasks {
    test {
        useJUnitPlatform()
    }

    register("javadocJar", Jar::class) {
        dependsOn(dokkaJavadoc)
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

nexusStaging {
    packageGroup = project.group as String
    stagingProfileId = System.getenv("SONATYPE_STAGING_PROFILE_ID")
    val stagingRepository = publishing.repositories["sonatype-staging"] as MavenArtifactRepository
    username = stagingRepository.credentials.username
    password = stagingRepository.credentials.password
}
