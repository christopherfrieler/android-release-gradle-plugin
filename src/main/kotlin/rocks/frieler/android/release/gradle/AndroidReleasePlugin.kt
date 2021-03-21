package rocks.frieler.android.release.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The Gradle [Plugin] class.
 */
class AndroidReleasePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val config = target.extensions.create("releasing", AndroidReleasePluginExtension::class.java)

        target.tasks.register("performRelease", PerformRelease::class.java) {
            it.scmRepository = config.scm(target)
        }
    }
}
