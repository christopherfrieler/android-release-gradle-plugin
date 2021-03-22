package rocks.frieler.android.release.gradle

import org.gradle.api.Action
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
        target.tasks.register("prepareNextDevelopmentVersion", PrepareNextDevelopmentVersion::class.java) {
            it.scmRepository = config.scm(target)
            it.appModule = config.appModule
        }
    }
}

/**
 * Configures the [AndroidReleasePlugin].
 */
fun Project.releasing(configureAction: Action<in AndroidReleasePluginExtension>) {
    this.extensions.configure("releasing", configureAction)
}
