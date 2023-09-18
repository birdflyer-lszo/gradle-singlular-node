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
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import static org.gradle.api.tasks.PathSensitivity.RELATIVE;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * Installs packages declared in the {@code package.json} file.
 */
public abstract class InstallPnpmPackagesTask
	extends DefaultTask
{
	private final ExecOperations processes;
	private final File packageFile;
	private final File lockFile;
	private final File executionMarker;

	@Inject
	public InstallPnpmPackagesTask(ExecOperations processes, Project project)
	{
		this.processes = processes;

		packageFile = project.file("package.json");
		lockFile = project.file("pnpm-lock.yaml");
		executionMarker = new File(project.file("node_modules"), ".install.executed");
	}

	/**
	 * Optional arguments to pass to the package installation command. By default, no arguments are defined.
	 */
	@Input
	@Optional
	public abstract ListProperty<CharSequence> getArgs();

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

	/**
	 * The execution marker file indicates that this task was executed. It is a compromise between reliability and
	 * speed. Declaring {@code node_modules} an output directory would add a tremendous hashing overhead for Gradle.
	 * <p>
	 * Manual changes to {@code node_modules} or changes introduced by a build cannot be detected with this approach.
	 */
	@OutputFile
	public File getExecutionMarkerFile()
	{
		return executionMarker;
	}

	@TaskAction
	public void installPackages()
		throws IOException
	{
		InstallationLayout layout = getInstallationLayout().get();
		String pnpmScript = layout.pathOfManagedPnpmScript().getAbsolutePath();

		new NodeCommand(processes, getWorkingDirectory().get().getAsFile(), layout)
			.args(pnpmScript, "install")
			.args(List.ofAll(getArgs().get()))
			.withEnvironment(HashMap.ofAll(System.getenv()))
			.execute();

		/*
		 * Just mark that this task was successful. Making node_modules an output directory would result in a large
		 * amount of time neeed to index that directory.
		 */
		if (!executionMarker.exists() && !executionMarker.createNewFile()) {
			throw new IllegalStateException("Failed to create execution marker");
		}
	}
}
