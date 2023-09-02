package com.brunoritz.gradle.singularnode.yarn;

import com.brunoritz.gradle.singularnode.platform.NodeCommand;
import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayout;
import io.vavr.collection.HashMap;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

/**
 * Installs the requested version of Yarn into the installation directory. This task uses the bundled version of NPM to
 * fetch the requested version of Yarn.
 * <p>
 * Any existing installation will be deleted prior to the installation.
 */
public abstract class InstallYarnTask
	extends DefaultTask
{
	private final FileSystemOperations files;
	private final ExecOperations processes;

	@Inject
	public InstallYarnTask(FileSystemOperations files, ExecOperations processes)
	{
		this.files = files;
		this.processes = processes;
	}

	@Input
	public abstract Property<CharSequence> getYarnVersion();

	@Internal
	public abstract DirectoryProperty getWorkingDirectory();

	@Internal
	public abstract Property<InstallationLayout> getInstallationLayout();

	@OutputDirectory
	public abstract DirectoryProperty getYarnInstallDirectory();

	@TaskAction
	public void installYarn()
	{
		cleanTarget();
		install();
	}

	private void cleanTarget()
	{
		InstallationLayout layout = getInstallationLayout().get();

		files.delete(spec -> spec.delete(layout.yarnInstallDirectory()));
	}

	private void install()
	{
		InstallationLayout layout = getInstallationLayout().get();
		String bundledNpm = layout.pathOfBundledNpmScript().getAbsolutePath();
		String npmPackage = String.format("yarn@%s", getYarnVersion().get());

		new NodeCommand(processes, getWorkingDirectory().get().getAsFile(), layout)
			.args(
				bundledNpm,
				"install",
				"--global",
				"--no-save",
				"--prefix", getYarnInstallDirectory().get().getAsFile().getAbsolutePath(),
				npmPackage
			)
			.withEnvironment(HashMap.ofAll(System.getenv()))
			.execute();
	}
}
