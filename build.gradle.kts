import com.vanniktech.maven.publish.DeploymentValidation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.3.20"
    id("org.jetbrains.dokka") version "2.2.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

repositories {
    mavenCentral()
}

group = "rocks.frieler.android"
version = "0.7.0-SNAPSHOT"

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
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

mavenPublishing {
    pom {
        name = project.name
        description = "A Gradle plugin to automate scm-releases of android apps."
        url = "https://github.com/christopherfrieler/android-release-gradle-plugin"
        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        scm {
            url = "https://github.com/christopherfrieler/android-release-gradle-plugin"
        }
        developers {
            developer { name = "Christopher Frieler" }
        }
    }
    publishToMavenCentral(
        automaticRelease = true,
        validateDeployment = DeploymentValidation.PUBLISHED)
}
