package rocks.frieler.android.release.gradle

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.lang.IllegalStateException


internal class ScmChecksTest {
    private val gitRepository = mock<GitRepository>()

    @Test
    internal fun `assertMasterBranch passes on branch master`() {
        whenever(gitRepository.getCurrentBranch()).thenReturn("master")

        gitRepository.assertMasterBranch()
    }

    @Test
    internal fun `assertMasterBranch fails on other branch than master`() {
        whenever(gitRepository.getCurrentBranch()).thenReturn("not-master")

        val exception = assertThrows<IllegalStateException> {
            gitRepository.assertMasterBranch()
        }

        assert(exception.message == "not on branch 'master'! current branch is 'not-master'.")
    }

    @Test
    internal fun `assertNoLocalChanges passes without local changes`() {
        whenever(gitRepository.hasLocalChanges()).thenReturn(false)

        gitRepository.assertNoLocalChanges()
    }

    @Test
    internal fun `assertNoLocalChanges fails when there are local changes`() {
        whenever(gitRepository.hasLocalChanges()).thenReturn(true)

        val exception = assertThrows<IllegalStateException> {
            gitRepository.assertNoLocalChanges()
        }

        assert(exception.message == "there are uncommitted changes!")
    }
}