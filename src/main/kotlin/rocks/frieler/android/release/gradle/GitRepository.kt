package rocks.frieler.android.release.gradle

import org.gradle.api.Project
import java.io.ByteArrayOutputStream


/**
 * Encapsulates git commands for a [Project] managed by git.
 */
class GitRepository(private val project: Project) {
    init {
        check(project.rootProject.file(".git").isDirectory) {
            "project doesn't seem to be a git repository, '.git' directory is missing!"
        }
    }

    fun getCurrentBranch() : String {
        val stdOut = ByteArrayOutputStream()
        project.exec {
            it.commandLine = "git branch --show-current".split(" ")
            it.standardOutput = stdOut
        }
        return stdOut.toString().trim()
    }

    fun hasLocalChanges() : Boolean {
        val hasUnstagedChanges = project.exec {
            it.commandLine = "git diff-files --quiet".split(" ")
            it.isIgnoreExitValue = true
        }.exitValue != 0
        val hasStagedChanges = project.exec {
            it.commandLine = "git diff-index HEAD --quiet".split(" ")
            it.isIgnoreExitValue = true
        }.exitValue != 0
        return hasUnstagedChanges || hasStagedChanges
    }

    fun commitAllChanges(commitMessage: String) {
        project.exec {
            it.commandLine = listOf("git", "commit", "-a", "-m", commitMessage)
        }
    }

    fun tag(tagName: String) {
        project.exec {
            it.commandLine = listOf("git", "tag", tagName)
        }
    }
}
