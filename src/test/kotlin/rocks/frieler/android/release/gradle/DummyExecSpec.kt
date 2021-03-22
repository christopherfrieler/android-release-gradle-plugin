package rocks.frieler.android.release.gradle

import org.gradle.process.BaseExecSpec
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.process.ExecSpec
import org.gradle.process.ProcessForkOptions
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class DummyExecSpec : ExecSpec {
    private var executable: Any? = null
    private var args = mutableListOf<String>()
    private val argumentProviders = mutableListOf<CommandLineArgumentProvider>()
    private lateinit var workingDir: File
    private var environmentVariables = mutableMapOf<String, Any?>()
    private var standardInput: InputStream? = null
    private var standardOutput: OutputStream? = null
    private var standardError: OutputStream? = null
    private var ignoreExitValue: Boolean = false

    override fun getExecutable() = executable?.toString()

    override fun setExecutable(executable: String?) {
        this.executable = executable
    }

    override fun setExecutable(executable: Any?) {
        this.executable = executable
    }

    override fun executable(executable: Any?): ProcessForkOptions {
        setExecutable(executable)
        return this
    }

    override fun getCommandLine(): MutableList<String?> {
        return mutableListOf<String?>().also { commandLine ->
            commandLine.add(getExecutable())
            commandLine.addAll(getArgs())
            argumentProviders.forEach { commandLine.addAll(it.asArguments()) }
        }
    }

    override fun setCommandLine(args: MutableList<String>?) {
        executable = args?.get(0)
        this.setArgs(args?.subList(1, args.size))
    }

    override fun setCommandLine(vararg args: Any?) {
        setCommandLine(args.toMutableList())
    }

    override fun setCommandLine(args: MutableIterable<*>?) {
        setCommandLine(args?.map { it.toString() }?.toMutableList())
    }

    override fun commandLine(vararg args: Any?): ExecSpec {
        setCommandLine(args)
        return this
    }

    override fun commandLine(args: MutableIterable<*>?): ExecSpec {
        setCommandLine(args)
        return this
    }

    override fun getWorkingDir() = workingDir

    override fun setWorkingDir(dir: File) {
        this.workingDir = dir
    }

    override fun setWorkingDir(dir: Any) {
        setWorkingDir(dir as File)
    }

    override fun workingDir(dir: Any): ProcessForkOptions {
        setWorkingDir(dir as File)
        return this
    }

    override fun getEnvironment() = environmentVariables

    override fun setEnvironment(environmentVariables: MutableMap<String, *>) {
        @Suppress("UNCHECKED_CAST")
        this.environmentVariables = environmentVariables as MutableMap<String, Any?>
    }

    override fun environment(environmentVariables: MutableMap<String, *>): ProcessForkOptions {
        @Suppress("UNCHECKED_CAST")
        environment = environmentVariables as MutableMap<String, Any?>
        return this
    }

    override fun environment(name: String, value: Any): ProcessForkOptions {
        environmentVariables[name] = value
        return this
    }

    override fun isIgnoreExitValue() = ignoreExitValue

    override fun setIgnoreExitValue(ignoreExitValue: Boolean): BaseExecSpec {
        this.ignoreExitValue = ignoreExitValue
        return this
    }

    override fun getStandardInput() = standardInput

    override fun setStandardInput(inputStream: InputStream?): BaseExecSpec {
        this.standardInput = inputStream
        return this
    }

    override fun getStandardOutput() = standardOutput

    override fun setStandardOutput(outputStream: OutputStream?): BaseExecSpec {
        this.standardOutput = outputStream
        return this
    }

    override fun getErrorOutput() = standardError

    override fun setErrorOutput(outputStream: OutputStream?): BaseExecSpec {
        this.standardError = outputStream
        return this
    }

    override fun getArgs() = args

    override fun args(vararg args: Any?): ExecSpec {
        this.args = args.map { it.toString() }.toMutableList()
        return this
    }

    override fun args(args: MutableIterable<*>?): ExecSpec {
        this.args = if (args == null) {
            mutableListOf()
        } else {
            args.map { it.toString() }.toMutableList()
        }
        return this
    }

    override fun setArgs(args: MutableList<String>?): ExecSpec {
        this.args = args ?: mutableListOf()
        return this
    }

    override fun setArgs(args: MutableIterable<*>?) = args(args)

    override fun getArgumentProviders() = argumentProviders

    override fun copyTo(target: ProcessForkOptions): ProcessForkOptions {
        target.setExecutable(executable)
        target.environment = environment
        target.workingDir = workingDir
        return this
    }
}