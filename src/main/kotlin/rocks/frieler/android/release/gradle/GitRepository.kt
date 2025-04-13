package rocks.frieler.android.release.gradle

import org.gradle.api.Project
import org.gradle.internal.extensions.core.serviceOf
import org.gradle.process.ExecOperations
import org.gradle.process.internal.DefaultExecOperations
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
        project.serviceOf<ExecOperations>().exec {
            it.commandLine = "git branch --show-current".split(" ")
            it.standardOutput = stdOut
        }
        return stdOut.toString().trim()
    }

    fun hasLocalChanges() : Boolean {
        val hasUnstagedChanges = project.serviceOf<ExecOperations>().exec {
            it.commandLine = "git diff-files --quiet".split(" ")
            it.isIgnoreExitValue = true
        }.exitValue != 0
        val hasStagedChanges = project.serviceOf<ExecOperations>().exec {
            it.commandLine = "git diff-index HEAD --quiet".split(" ")
            it.isIgnoreExitValue = true
        }.exitValue != 0
        return hasUnstagedChanges || hasStagedChanges
    }

    fun commitAllChanges(commitMessage: String) {
        project.serviceOf<ExecOperations>().exec {
            it.commandLine = listOf("git", "commit", "-a", "-m", commitMessage)
        }
    }

    fun tag(tagName: String) {
        project.serviceOf<ExecOperations>().exec {
            it.commandLine = listOf("git", "tag", tagName)
        }
    }
}
