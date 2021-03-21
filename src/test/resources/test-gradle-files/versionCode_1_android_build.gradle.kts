plugins {
    id("com.android.application")
}

android {
    defaultConfig {
        applicationId = "rocks.frieler.testapp"
        minSdkVersion(1)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "${project.version}"
    }
}

dependencies {
}
