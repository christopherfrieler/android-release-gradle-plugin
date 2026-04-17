package rocks.frieler.android.release.gradle

import org.gradle.process.ExecResult
import org.gradle.process.ProcessExecutionException
import java.lang.IllegalArgumentException

class DummyExecResult private constructor(
    private val exitValue: Int,
    private val failure: ProcessExecutionException?,
) : ExecResult {

    override fun getExitValue() = exitValue

    override fun assertNormalExitValue(): ExecResult {
        if (exitValue != 0) {
            throw ProcessExecutionException("process exited with code $exitValue")
        }
        return this
    }

    override fun rethrowFailure(): ExecResult {
        if (failure != null) {
            throw failure
        }
        return this
    }

    companion object {
        fun success() = DummyExecResult(0, null)
        fun failure(exitValue: Int, failure: ProcessExecutionException? = null): DummyExecResult {
            if (exitValue == 0) {
                throw IllegalArgumentException("exitValue for failed process must not be 0.")
            }
            return DummyExecResult(exitValue, failure)
        }
    }
}
