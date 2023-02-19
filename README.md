# android-release-gradle-plugin
[![Maven Central](https://img.shields.io/maven-central/v/rocks.frieler.android/android-release-gradle-plugin.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22rocks.frieler.android%22%20AND%20a:%22android-release-gradle-plugin%22)

A Gradle plugin to automate scm-releases of Android apps

**Note:** This plugin was created together with certain Android app projects.
Hence, it is tailored exactly for their way of releasing and versioning and offers only little configuration options at the moment.
However, if it does not fit your needs, you're welcome to [open a feature request](https://github.com/christopherfrieler/android-release-gradle-plugin/issues).

## Usage
Creating a new release of your Android app is performed in two steps performed on the `master`-branch.
The plugin adds two Gradle tasks to your project, one for each of these steps:

`gradle performRelease` expects a semantic SNAPSHOT-version of the form "_major_._minor_._fix_-SNAPSHOT".
It will remove the "-SNAPSHOT" suffix from the version, commit this change and tag that commit with "_major_._minor_._fix_".

Afterwards `gradle prepareNextDevelopmentVersion` prepares the next SNAPSHOT-version by incrementing the _minor_-, resetting the _fix_-version and appending the "-SNAPSHOT" suffix.
It also increments the `versionCode` of the app-module.
Furthermore, it clears the changelog in '_app-module_/src/main/play/release-notes/de-DE/default.txt' as specified by the [Gradle Play Publisher plugin](https://github.com/Triple-T/gradle-play-publisher).
These changes are committed again.

### Applying the plugin in your Android project
Make sure to add Maven Central to pull the plugin from in your 'settings.gradle.kts':
```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        //...
    }
}
```

Then in your root 'build.gradle.kts' apply the plugin:
```kotlin
plugins {
    id("rocks.frieler.android.release") version "0.3.0"
}
```

You can configure the plugin using the `releasing`-extension again in your root 'build.gradle.kts':
```kotlin
releasing {
    appModule = "my-app" // Name of the actual app-module, defaults to "app".
}
```

## Contributing
If you have a feature idea, problem or question you can open an [issue](https://github.com/christopherfrieler/android-release-gradle-plugin/issues).
If you want to work on this plugin to contribute a PR, have a look at the [development docs](DEVELOPING.md).
