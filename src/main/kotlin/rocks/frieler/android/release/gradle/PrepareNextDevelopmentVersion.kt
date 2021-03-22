package rocks.frieler.android.release.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Gradle-[org.gradle.api.Task] to prepare the next development version, i.e.
 * - increase the minor-version, reset the fix-version and append the '-SNAPSHOT'-suffix to the version specified in
 * the build-file
 * - increase the Android version code in the app-module
 * - clear the changelog file
 * - commit the changes
 */
open class PrepareNextDevelopmentVersion : DefaultTask() {
    @get:Input
    internal lateinit var scmRepository: GitRepository

    @Input
    var appModule: String = "app"

    init {
        group = "versioning"
        description = """
            Prepares the next SNAPSHOT version and increases the app's version code.
        """.trimIndent()
    }

    @TaskAction
    fun prepareNextDevelopmentVersion() {
        scmRepository.assertMasterBranch()
        scmRepository.assertNoLocalChanges()

        val nextDevelopmentVersion = prepareNextSnapshotVersion()
        increaseVersionCode()
        clearChangeLog()
        scmRepository.commitAllChanges("prepare $nextDevelopmentVersion")
    }

    private fun prepareNextSnapshotVersion(): String {
        var nextSnapshotVersion: String? = null

        val newGradleFile = StringBuilder()
        val versionSpecificationPattern = Regex("^\\s*version\\s*=\\s*\"([^\"]*)\"\\s*$")
        project.file("./build.gradle.kts").readLines().forEach { line ->
            val match = versionSpecificationPattern.matchEntire(line)
            if (match != null) {
                val currentVersion = match.groups[1]!!.value
                val versionPattern = Regex("^(\\d+)\\.(\\d+)\\.\\d+$")
                val versionMatch = versionPattern.matchEntire(currentVersion)
                check(versionMatch != null) { "current version '$currentVersion' does not match pattern <major>.<minor>.<fix>!" }
                val major = versionMatch.groups[1]!!.value.toInt()
                val minor = versionMatch.groups[2]!!.value.toInt()
                nextSnapshotVersion = "$major.${minor + 1}.0-SNAPSHOT"
                newGradleFile.appendLineWithSystemEnding(match.value.replace(currentVersion, nextSnapshotVersion!!))
            } else {
                newGradleFile.appendLineWithSystemEnding(line)
            }
        }
        project.file("./build.gradle.kts").writeText(newGradleFile.toString())

        check(nextSnapshotVersion != null) { "no version specification found!" }
        return nextSnapshotVersion!!
    }

    private fun increaseVersionCode() {
        val newGradleFile = StringBuilder()
        val versionCodeSpecificationPattern = Regex("^\\s*versionCode\\s*=\\s*(\\d+)\\s*$")
        project.file("$appModule/build.gradle.kts").readLines().forEach { line ->
            val versionCodeSpecificationMatch = versionCodeSpecificationPattern.matchEntire(line)
            if (versionCodeSpecificationMatch != null) {
                val currentVersionCode = versionCodeSpecificationMatch.groups[1]!!.value.toInt()
                newGradleFile.appendLineWithSystemEnding(versionCodeSpecificationMatch.value.replace(currentVersionCode.toString(), (currentVersionCode + 1).toString()))
            } else {
                newGradleFile.appendLineWithSystemEnding(line)
            }
        }
        project.file("$appModule/build.gradle.kts").writeText(newGradleFile.toString())
    }

    private fun clearChangeLog() {
        project.file("$appModule/src/main/play/release-notes/de-DE/default.txt").writeText("")
    }
}
