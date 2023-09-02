package com.brunoritz.gradle.singularnode.npm;

import com.brunoritz.gradle.singularnode.NodeJsExtension;
import com.brunoritz.gradle.singularnode.nodejs.InstallNodeJsTask;
import com.brunoritz.gradle.singularnode.platform.Lookup;
import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayout;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import static com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory.platformDependentLayout;

/**
 * Configures all required tasks and properties for the NPM package manager.
 */
public final class NpmSetup
{
	private static final String GROUP = "NPM";

	private NpmSetup()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Configures the tasks needed to install the NPM package manager on the root project. The setup task itself will
	 * depend on the one responsible for setting up NodeJS itself.
	 *
	 * @param project
	 * 	The root project on which to register the setup task
	 * @param nodeInstallationTask
	 * 	The task that installs NodeJS itself
	 */
	public static void setupRootTasks(Project project, TaskProvider<InstallNodeJsTask> nodeInstallationTask)
	{
		NodeJsExtension configuration = Lookup.pluginConfiguration(project)
			.getOrElseThrow(() -> new IllegalStateException("Plugin configuration does not exist in root project"));
		InstallationLayout layout = platformDependentLayout(configuration.installBaseDir)
			.getOrElseThrow(() -> new IllegalStateException("Running on unsupported OS"));
		TaskProvider<InstallNpmTask> npmInstallationTask =
			project.getTasks().register("installNpm", InstallNpmTask.class);

		npmInstallationTask.configure(task -> {
			task.setGroup(GROUP);
			task.dependsOn(nodeInstallationTask);
			task.getInstallationLayout().set(layout);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getNpmInstallDirectory().set(layout.npmInstallDirectory());
			task.getNpmVersion().set(configuration.npmVersion);
		});
	}

	/**
	 * Configures the tasks for any subproject using this plugin. A task for installing packages via NPM will be
	 * registered {@code installNpmPackages}. Any consumer defined task of type {@link NpmTask} will automatically
	 * be made dependent on the {@code installNpmPackages} task.
	 *
	 * @param project
	 * 	The subproject to configure
	 */
	public static void setupChildTasks(Project project)
	{
		NodeJsExtension configuration = Lookup.pluginConfiguration(project)
			.getOrElseThrow(() -> new IllegalStateException("Plugin configuration does not exist in root project"));
		InstallationLayout layout = platformDependentLayout(configuration.installBaseDir)
			.getOrElseThrow(() -> new IllegalStateException("Running on unsupported OS"));
		TaskProvider<Task> npmSetupTask = Lookup.rootProjectTask(project, "installNpm")
			.getOrElseThrow(() -> new IllegalStateException("Requested task does not exist on root project"));
		TaskProvider<InstallNpmPackagesTask> installNpmPackagesTask =
			project.getTasks().register("installNpmPackages", InstallNpmPackagesTask.class);

		installNpmPackagesTask.configure(task -> {
			task.setGroup(GROUP);
			task.dependsOn(npmSetupTask);

			task.getArgs().set(configuration.npmInstallArgs);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getInstallationLayout().set(layout);
		});

		project.getTasks().whenTaskAdded(newTask -> {
			if (newTask instanceof NpmTask newNpmTask) {
				newNpmTask.dependsOn(installNpmPackagesTask);
				newNpmTask.getWorkingDirectory().set(project.getProjectDir());
				newNpmTask.getInstallBaseDir().set(configuration.installBaseDir);
				newNpmTask.getInstallationLayout().set(layout);
			}
		});

		project.getExtensions().getExtraProperties().set("NpmTask", NpmTask.class);
	}
}
