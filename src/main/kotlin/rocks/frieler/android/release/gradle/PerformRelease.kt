package rocks.frieler.android.release.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


/**
 * Gradle-[org.gradle.api.Task] to perform a release, i.e. remove the '-SNAPSHOT'-suffix from the version specified in
 * the build-file, commit and tag this change.
 */
open class PerformRelease : DefaultTask() {

    @get:Input
    internal lateinit var scmRepository: GitRepository

    init {
        group = "versioning"
        description = """
            Performs a release by removing the "-SNAPSHOT"-suffix from the version name.
        """.trimIndent()
    }

    @TaskAction
    fun performRelease() {
        scmRepository.assertMasterBranch()
        scmRepository.assertNoLocalChanges()

        val releaseVersion = unsnapshotVersion()
        scmRepository.commitAllChanges("\"release $releaseVersion\"")
        scmRepository.tag("\"$releaseVersion\"")
    }

    private fun unsnapshotVersion(): String {
        var releaseVersion: String? = null

        val newGradleFile = StringBuilder()
        val versionSpecificationPattern = Regex("^\\s*version\\s*=\\s*\"([^\"]*)\"\\s*$")
        project.file("./build.gradle.kts").readLines().forEach {
            val match = versionSpecificationPattern.matchEntire(it)
            if (match != null) {
                val currentVersion = match.groups[1]!!.value
                check(currentVersion.endsWith("-SNAPSHOT")) { "current version '$currentVersion' is not a SNAPSHOT!" }
                releaseVersion = currentVersion.replace("-SNAPSHOT", "")
                newGradleFile.appendLine(match.value.replace(currentVersion, releaseVersion!!))
            } else {
                newGradleFile.appendLine(it)
            }
        }
        project.file("./build.gradle.kts").writeText(newGradleFile.toString())

        check(releaseVersion != null) { "no version specification found!" }
        return releaseVersion!!
    }
}