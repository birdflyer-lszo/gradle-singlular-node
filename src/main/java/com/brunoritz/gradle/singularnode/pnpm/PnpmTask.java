package com.brunoritz.gradle.singularnode.pnpm;

import com.brunoritz.gradle.singularnode.platform.NodeCommand;
import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayout;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import static org.gradle.api.tasks.PathSensitivity.RELATIVE;

import javax.inject.Inject;
import java.io.File;

/**
 * The task type for defining custon PNPM tasks to execute. Any task that uses this class as its type will automatically
 * depend on the package installation task to enusure up-to-date packages.
 *
 * <b>Example Usage</b>
 * <pre>
 * // package.json
 * {
 *     "scripts": {
 *         "test": "node -e 'console.log(process.env)'"
 *     }
 * }
 *
 * // build.gradle
 * task runTest(type: PnpmTask) {
 *     args.set([
 *         'run', 'test'
 *     ])
 * }
 * </pre>
 */
public abstract class PnpmTask
	extends DefaultTask
{
	private final ExecOperations processes;
	private final File packageFile;
	private final File lockFile;

	@Inject
	public PnpmTask(ExecOperations processes, Project project)
	{
		this.processes = processes;

		packageFile = project.file("package.json");
		lockFile = project.file("pnpm-lock.yaml");
	}

	@Input
	@Optional
	public abstract ListProperty<CharSequence> getArgs();

	@Input
	@Optional
	public abstract MapProperty<CharSequence, CharSequence> getEnvironment();

	@Internal
	public abstract DirectoryProperty getInstallBaseDir();

	@Internal
	public abstract DirectoryProperty getWorkingDirectory();

	@Internal
	public abstract Property<InstallationLayout> getInstallationLayout();

	@InputFile
	@PathSensitive(RELATIVE)
	@Optional
	@Nullable
	public File getPackageFile()
	{
		return packageFile.exists() ? packageFile : null;
	}

	@InputFile
	@PathSensitive(RELATIVE)
	@Optional
	@Nullable
	public File getPackageLockFile()
	{
		return lockFile.exists() ? lockFile : null;
	}

	@TaskAction
	public void execute()
	{
		InstallationLayout layout = getInstallationLayout().get();
		String pnpmScript = layout.pathOfManagedPnpmScript().getAbsolutePath();

		new NodeCommand(processes, getWorkingDirectory().get().getAsFile(), layout)
			.args(pnpmScript)
			.args(List.ofAll(getArgs().get()))
			.withEnvironment(HashMap.ofAll(System.getenv()))
			.withEnvironment(HashMap.ofAll(getEnvironment().get()))
			.execute();
	}
}
