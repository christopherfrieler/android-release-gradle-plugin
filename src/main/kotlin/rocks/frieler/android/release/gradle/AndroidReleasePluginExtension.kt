package rocks.frieler.android.release.gradle

import org.gradle.api.Project

open class AndroidReleasePluginExtension {
    var scm: (Project) -> GitRepository = { GitRepository(it) }
}
