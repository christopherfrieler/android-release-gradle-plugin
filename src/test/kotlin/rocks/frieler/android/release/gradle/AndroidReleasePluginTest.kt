package rocks.frieler.android.release.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class AndroidReleasePluginTest {

    @Test
    fun `plugin can be applied to a Gradle project by id`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply("rocks.frieler.android.release")

        assertNotNull(project.plugins.findPlugin(AndroidReleasePlugin::class.java))
    }

    @Test
    internal fun `plugin adds extension to configure it`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply("rocks.frieler.android.release")

        assert(project.extensions.findByName("releasing") is AndroidReleasePluginExtension)
    }
}