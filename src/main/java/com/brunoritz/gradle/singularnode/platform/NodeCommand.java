package com.brunoritz.gradle.singularnode.platform;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.gradle.process.ExecOperations;

import java.io.File;
import java.util.Arrays;

/**
 * Utility to create NodeJS invocations. This class provides means of setting the execution environment and providing
 * any arguments necessary. The commands are executed using th calling project's directory as its working directory.
 */
public class NodeCommand
{
	private final ExecOperations processes;
	private final InstallationLayout layout;
	private final List<CharSequence> args;
	private final Map<CharSequence, CharSequence> environment;
	private final File workingDirectory;

	public NodeCommand(ExecOperations processes, File workingDirectory, InstallationLayout layout)
	{
		this(processes, layout, workingDirectory, List.of(), HashMap.empty());
	}

	private NodeCommand(
		ExecOperations processes,
		InstallationLayout layout,
		File workingDirectory,
		List<CharSequence> args,
		Map<CharSequence, CharSequence> environment)
	{
		this.processes = processes;
		this.layout = layout;
		this.workingDirectory = workingDirectory;
		this.args = args;
		this.environment = environment;
	}

	/**
	 * Appends the given arguments to the potentially existing ones. No existing parameters will be overwritten.
	 *
	 * @param args
	 * 	The additional arguments
	 *
	 * @return A new instance with the updated arguments
	 */
	public NodeCommand args(CharSequence... args)
	{
		return args(List.ofAll(Arrays.asList(args)));
	}

	/**
	 * Appends the given arguments to the potentially existing ones. No existing parameters will be overwritten.
	 *
	 * @param args
	 * 	The additional arguments
	 *
	 * @return A new instance with the updated arguments
	 */
	public NodeCommand args(List<CharSequence> args)
	{
		List<CharSequence> mergedArgs = this.args.appendAll(args);

		return new NodeCommand(processes, layout, workingDirectory, mergedArgs, environment);
	}

	/**
	 * Appends environment variables to the command. Existing environment variables will be overwritten with those
	 * contained in {@code environment}.
	 *
	 * @param environment
	 * 	The additional environment to apply
	 *
	 * @return A new instance with the updated environment
	 */
	public NodeCommand withEnvironment(Map<CharSequence, CharSequence> environment)
	{
		Map<CharSequence, CharSequence> mergedEnvironment = this.environment;

		for (Tuple2<CharSequence, CharSequence> envVariable : environment) {
			mergedEnvironment = mergedEnvironment.put(envVariable);
		}

		return new NodeCommand(processes, layout, workingDirectory, args, mergedEnvironment);
	}

	/**
	 * Executes the configured command. In order to make the NodeJS invocation work properly, the {@code PATH}
	 * envioronemnt variable is prepended with the binary directory of the NodeJS installation.
	 */
	public void execute()
	{
		processes.exec(exec -> {
			String nodeExecutable = layout.pathOfNodeExecutable().getAbsolutePath();

			exec.setExecutable(nodeExecutable);
			exec.setArgs(args.toJavaList());
			exec.setWorkingDir(workingDirectory);
			exec.environment(appendNodeToPathToEnvironment().toJavaMap());
		});
	}

	private Map<String, String> appendNodeToPathToEnvironment()
	{
		String existingPath = environment.getOrElse("PATH", "").toString();
		String newPath = String.format("%s%s%s",
			layout.nodeJsBinDirectory().getAbsolutePath(), File.pathSeparator, existingPath
		);

		return environment.put("PATH", newPath)
			.bimap(CharSequence::toString, CharSequence::toString);
	}
}
