package rocks.frieler.android.release.gradle

import org.gradle.api.internal.AbstractTask.injectIntoNewInstance
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

internal class PrepareNextDevelopmentVersionTest {
    private val project: ProjectInternal = mock()
    private lateinit var prepareNextDevelopmentVersionTask: PrepareNextDevelopmentVersion

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
        prepareNextDevelopmentVersionTask = injectIntoNewInstance(
            project,
            TaskIdentityFactory(idFactory).create("prepareNextDevelopmentVersion", PrepareNextDevelopmentVersion::class.java, project)
        ) {
            PrepareNextDevelopmentVersion()
        }
        prepareNextDevelopmentVersionTask.scmRepository = mock()
    }

    @Test
    internal fun `prepareNextDevelopmentVersion prepares next minor SNAPSHOT version and versioncode, clears the changelog and commits it all`() {
        whenever(prepareNextDevelopmentVersionTask.scmRepository.getCurrentBranch()).thenReturn("master")
        whenever(prepareNextDevelopmentVersionTask.scmRepository.hasLocalChanges()).thenReturn(false)
        val testRootBuildFile = prepareTestBuildFile("1.0.0_build.gradle.kts")
        whenever(project.file("./build.gradle.kts")).thenReturn(testRootBuildFile)
        val testAppBuildFile = prepareTestBuildFile("versionCode_1_android_build.gradle.kts")
        whenever(project.file("app/build.gradle.kts")).thenReturn(testAppBuildFile)
        val testChangelogFile = Files.createTempFile("changelog", "txt").toFile().apply {
            writeText("There was a change.${System.lineSeparator()}")
        }
        whenever(project.file("app/src/main/play/release-notes/de-DE/default.txt")).thenReturn(testChangelogFile)

        prepareNextDevelopmentVersionTask.prepareNextDevelopmentVersion()

        assert(testRootBuildFile.readText().contains("version = \"1.1.0-SNAPSHOT\"${System.lineSeparator()}"))
        assert(testAppBuildFile.readText().contains("versionCode = 2${System.lineSeparator()}"))
        assert(testChangelogFile.readText() == "")
        verify(prepareNextDevelopmentVersionTask.scmRepository).commitAllChanges("prepare 1.1.0-SNAPSHOT")
    }

    @Test
    internal fun `prepareNextDevelopmentVersionTask fails when executed on another branch than master`() {
        whenever(prepareNextDevelopmentVersionTask.scmRepository.getCurrentBranch()).thenReturn("not-master")

        assertThrows<IllegalStateException> {
            prepareNextDevelopmentVersionTask.prepareNextDevelopmentVersion()
        }
    }

    @Test
    internal fun `prepareNextDevelopmentVersionTask fails when there are local changes`() {
        whenever(prepareNextDevelopmentVersionTask.scmRepository.getCurrentBranch()).thenReturn("master")
        whenever(prepareNextDevelopmentVersionTask.scmRepository.hasLocalChanges()).thenReturn(true)

        assertThrows<IllegalStateException> {
            prepareNextDevelopmentVersionTask.prepareNextDevelopmentVersion()
        }
    }

    @Test
    fun `prepareNextDevelopmentVersion fails when current version is already a SNAPSHOT`() {
        whenever(prepareNextDevelopmentVersionTask.scmRepository.getCurrentBranch()).thenReturn("master")
        whenever(prepareNextDevelopmentVersionTask.scmRepository.hasLocalChanges()).thenReturn(false)
        val testBuildFile = prepareTestBuildFile("1.0.0-SNAPSHOT_build.gradle.kts")
        whenever(project.file("./build.gradle.kts")).thenReturn(testBuildFile)

        val illegalStateException = assertThrows<java.lang.IllegalStateException> {
            prepareNextDevelopmentVersionTask.prepareNextDevelopmentVersion()
        }

        assert(illegalStateException.message == "current version '1.0.0-SNAPSHOT' does not match pattern <major>.<minor>.<fix>!")
    }

    @Test
    fun `prepareNextDevelopmentVersion fails when there is no version specification`() {
        whenever(prepareNextDevelopmentVersionTask.scmRepository.getCurrentBranch()).thenReturn("master")
        whenever(prepareNextDevelopmentVersionTask.scmRepository.hasLocalChanges()).thenReturn(false)
        val testBuildFile = prepareTestBuildFile("no_version_build.gradle.kts")
        whenever(project.file("./build.gradle.kts")).thenReturn(testBuildFile)

        val illegalStateException = assertThrows<java.lang.IllegalStateException> {
            prepareNextDevelopmentVersionTask.prepareNextDevelopmentVersion()
        }

        assert(illegalStateException.message == "no version specification found!")
    }

    private fun prepareTestBuildFile(buildFile: String): File {
        return Files.createTempFile("build", "gradle.kts").toFile().apply {
            val testBuildFile = File(this@PrepareNextDevelopmentVersionTest.javaClass.getResource("/test-gradle-files/$buildFile").toURI())
            testBuildFile.copyTo(this, overwrite = true)
        }
    }
}