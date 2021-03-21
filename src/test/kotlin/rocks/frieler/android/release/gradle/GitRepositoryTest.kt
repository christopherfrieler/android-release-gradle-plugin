package rocks.frieler.android.release.gradle

import com.nhaarman.mockitokotlin2.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.process.ExecSpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.charset.Charset

internal class GitRepositoryTest {
    private val project: Project = mockGitManagedProject()

    private val gitRepository = GitRepository(project)

    @Test
    internal fun `can determine current branch`() {
        whenever(project.exec(any<Action<in ExecSpec>>())).thenAnswer {
            val execSpec = DummyExecSpec()
            it.getArgument<Action<in ExecSpec>>(0).execute(execSpec)
            when(execSpec.commandLine) {
                listOf("git", "branch", "--show-current") -> {
                    execSpec.standardOutput!!.write("branch".toByteArray(Charset.defaultCharset()))
                    return@thenAnswer DummyExecResult.success()
                }
                else -> fail<DummyExecResult>("unexpected command-line invocation")
            }
        }

        val currentBranch = gitRepository.getCurrentBranch()

        assertEquals("branch", currentBranch)
    }

    @Test
    internal fun `can check for no local changes`() {
        whenever(project.exec(any<Action<in ExecSpec>>())).thenAnswer {
            val execSpec = DummyExecSpec()
            it.getArgument<Action<in ExecSpec>>(0).execute(execSpec)
            when {
                execSpec.commandLine == listOf("git", "diff-files", "--quiet") && execSpec.isIgnoreExitValue -> {
                    return@thenAnswer DummyExecResult.success()
                }
                execSpec.commandLine == listOf("git", "diff-index", "HEAD", "--quiet") && execSpec.isIgnoreExitValue -> {
                    return@thenAnswer DummyExecResult.success()
                }
                else -> fail<DummyExecResult>("unexpected command-line invocation")
            }
        }

        val hasLocalChanges = gitRepository.hasLocalChanges()

        assert(!hasLocalChanges)
    }

    @Test
    internal fun `detects unstaged local changes`() {
        whenever(project.exec(any<Action<in ExecSpec>>())).thenAnswer {
            val execSpec = DummyExecSpec()
            it.getArgument<Action<in ExecSpec>>(0).execute(execSpec)
            when {
                execSpec.commandLine == listOf("git", "diff-files", "--quiet") && execSpec.isIgnoreExitValue -> {
                    return@thenAnswer DummyExecResult.failure(1)
                }
                execSpec.commandLine == listOf("git", "diff-index", "HEAD", "--quiet") && execSpec.isIgnoreExitValue -> {
                    return@thenAnswer DummyExecResult.success()
                }
                else -> fail<DummyExecResult>("unexpected command-line invocation")
            }
        }

        val hasLocalChanges = gitRepository.hasLocalChanges()

        assert(hasLocalChanges)
    }

    @Test
    internal fun `detects staged local changes`() {
        whenever(project.exec(any<Action<in ExecSpec>>())).thenAnswer {
            val execSpec = DummyExecSpec()
            it.getArgument<Action<in ExecSpec>>(0).execute(execSpec)
            when {
                execSpec.commandLine == listOf("git", "diff-files", "--quiet") && execSpec.isIgnoreExitValue -> {
                    return@thenAnswer DummyExecResult.success()
                }
                execSpec.commandLine == listOf("git", "diff-index", "HEAD", "--quiet") && execSpec.isIgnoreExitValue -> {
                    return@thenAnswer DummyExecResult.failure(1)
                }
                else -> fail<DummyExecResult>("unexpected command-line invocation")
            }
        }

        val hasLocalChanges = gitRepository.hasLocalChanges()

        assert(hasLocalChanges)
    }

    @Test
    internal fun `can commit all changes`() {
        val message = "release-commit"
        whenever(project.exec(any<Action<in ExecSpec>>())).thenAnswer {
            val execSpec = DummyExecSpec()
            it.getArgument<Action<in ExecSpec>>(0).execute(execSpec)
            when(execSpec.commandLine) {
                listOf("git", "commit", "-a", "-m", message) -> {
                    return@thenAnswer DummyExecResult.success()
                }
                else -> fail<DummyExecResult>("unexpected command-line invocation")
            }
        }

        gitRepository.commitAllChanges(message)

        verify(project).exec(commandWith { commandLine == listOf("git", "commit", "-a", "-m", message) })
    }

    @Test
    internal fun `can tag HEAD commit`() {
        val tagName = "new tag"
        whenever(project.exec(any<Action<in ExecSpec>>())).thenAnswer {
            val execSpec = DummyExecSpec()
            it.getArgument<Action<in ExecSpec>>(0).execute(execSpec)
            when(execSpec.commandLine) {
                listOf("git", "tag", tagName) -> {
                    return@thenAnswer DummyExecResult.success()
                }
                else -> fail<DummyExecResult>("unexpected command-line invocation")
            }
        }

        gitRepository.tag(tagName)

        verify(project).exec(commandWith { commandLine == listOf("git", "tag", tagName) })
    }

    private fun mockGitManagedProject(): Project {
        return mock {
            on { rootProject } doReturn this.mock
            val dotGitDirectory = mock<File> { on { isDirectory } doReturn true }
            on { file(".git") } doReturn dotGitDirectory
        }
    }

    private fun commandWith(matcher: ExecSpec.() -> Boolean) =
        argThat<Action<in ExecSpec>> { configurer ->
            val execSpec = DummyExecSpec().apply { configurer.execute(this@apply) }
            return@argThat matcher(execSpec)
        }
}
