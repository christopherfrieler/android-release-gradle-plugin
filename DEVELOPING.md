# Developing on android-release-gradle-plugin

It's a [Gradle](https://docs.gradle.org/current/userguide/userguide.html) project using the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html), so you can use the standard gradle commands:

Build the plugin: `./gradlew build`

Run unit tests: `./gradlew test`

Package the sources-jar: `./gradlew kotlinSourcesJar`

Package the kdoc-jar: `./gradlew kdocJar`

Publish to your local maven repository: `./gradlew publishToMavenLocal`.
This way you can test the plugin manually in another project by pulling your current SNAPSHOT from your local repository.
