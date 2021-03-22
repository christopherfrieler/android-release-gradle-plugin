package rocks.frieler.android.release.gradle

import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException
import java.lang.IllegalArgumentException

class DummyExecResult private constructor(
    private val exitValue: Int,
    private val failure: ExecException?,
) : ExecResult {

    override fun getExitValue() = exitValue

    override fun assertNormalExitValue(): ExecResult {
        if (exitValue != 0) {
            throw ExecException("process exited with code $exitValue")
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
        fun failure(exitValue: Int, failure: ExecException? = null): DummyExecResult {
            if (exitValue == 0) {
                throw IllegalArgumentException("exitValue for failed process must not be 0.")
            }
            return DummyExecResult(exitValue, failure)
        }
    }
}
