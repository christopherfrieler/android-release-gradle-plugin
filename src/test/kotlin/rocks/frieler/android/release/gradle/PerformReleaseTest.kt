package rocks.frieler.android.release.gradle

import org.gradle.api.DefaultTask.injectIntoNewInstance
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.project.taskfactory.TaskIdentityFactory
import org.gradle.api.internal.tasks.TaskDependencyFactory
import org.gradle.api.internal.tasks.TaskExecutionAccessChecker
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.id.ConfigurationCacheableIdFactory
import org.gradle.internal.service.ServiceRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.nio.file.Files


internal class PerformReleaseTest {
    private val project: ProjectInternal = mock()
    private lateinit var performReleaseTask: PerformRelease

    @BeforeEach
    fun initTaskWithMockedProject() {
        val mockGradleInvocation = mock<GradleInternal>()
        val mockServiceregistry = mock<ServiceRegistry>()
        whenever(mockServiceregistry.get(TaskExecutionAccessChecker::class.java)).thenReturn(mock())
        whenever(project.services).thenReturn(mockServiceregistry)
        whenever(project.gradle).thenReturn(mockGradleInvocation)
        val mockObjectFactory = mock<ObjectFactory>()
        whenever(project.objects).thenReturn(mockObjectFactory)
        val taskDependencyFactory = mock<TaskDependencyFactory>()
        whenever(taskDependencyFactory.configurableDependency()).thenReturn(mock())
        whenever(project.taskDependencyFactory).thenReturn(taskDependencyFactory)
        val idFactory = mock<ConfigurationCacheableIdFactory>()
        whenever(idFactory.createId()).thenReturn(1L).thenThrow(IllegalStateException())
        performReleaseTask = injectIntoNewInstance(
            project,
            TaskIdentityFactory(idFactory).create("performRelease", PerformRelease::class.java, project)
        ) {
            PerformRelease()
        }
        performReleaseTask.scmRepository = mock()
    }

    @Test
    fun `performRelease removes SNAPSHOT suffix from version in build file, commits and tags it`() {
        whenever(performReleaseTask.scmRepository.getCurrentBranch()).thenReturn("master")
        whenever(performReleaseTask.scmRepository.hasLocalChanges()).thenReturn(false)
        val testBuildFile = prepareTestBuildFile("1.0.0-SNAPSHOT_build.gradle.kts")
        whenever(project.file("./build.gradle.kts")).thenReturn(testBuildFile)

        performReleaseTask.performRelease()

        assert(testBuildFile.readText().contains("version = \"1.0.0\"${System.lineSeparator()}"))
        verify(performReleaseTask.scmRepository).commitAllChanges("\"release 1.0.0\"")
        verify(performReleaseTask.scmRepository).tag("\"1.0.0\"")
    }

    @Test
    internal fun `performRelease fails when executed on another branch than master`() {
        whenever(performReleaseTask.scmRepository.getCurrentBranch()).thenReturn("not-master")

        assertThrows<IllegalStateException> {
            performReleaseTask.performRelease()
        }
    }

    @Test
    internal fun `performRelease fails when there are local changes`() {
        whenever(performReleaseTask.scmRepository.getCurrentBranch()).thenReturn("master")
        whenever(performReleaseTask.scmRepository.hasLocalChanges()).thenReturn(true)

        assertThrows<IllegalStateException> {
            performReleaseTask.performRelease()
        }
    }

    @Test
    fun `performRelease fails when current version is not SNAPSHOT`() {
        whenever(performReleaseTask.scmRepository.getCurrentBranch()).thenReturn("master")
        whenever(performReleaseTask.scmRepository.hasLocalChanges()).thenReturn(false)
        val testBuildFile = prepareTestBuildFile("1.0.0_build.gradle.kts")
        whenever(project.file("./build.gradle.kts")).thenReturn(testBuildFile)

        val illegalStateException = assertThrows<java.lang.IllegalStateException> {
            performReleaseTask.performRelease()
        }

        assert(illegalStateException.message == "current version '1.0.0' is not a SNAPSHOT!")
    }

    @Test
    fun `performRelease fails when there is no version specification`() {
        whenever(performReleaseTask.scmRepository.getCurrentBranch()).thenReturn("master")
        whenever(performReleaseTask.scmRepository.hasLocalChanges()).thenReturn(false)
        val testBuildFile = prepareTestBuildFile("no_version_build.gradle.kts")
        whenever(project.file("./build.gradle.kts")).thenReturn(testBuildFile)

        val illegalStateException = assertThrows<java.lang.IllegalStateException> {
            performReleaseTask.performRelease()
        }

        assert(illegalStateException.message == "no version specification found!")
    }

    private fun prepareTestBuildFile(buildFile: String): File {
        return Files.createTempFile("build", "gradle.kts").toFile().apply {
            val testBuildFile = File(this@PerformReleaseTest.javaClass.getResource("/test-gradle-files/$buildFile").toURI())
            testBuildFile.copyTo(this, overwrite = true)
        }
    }
}
