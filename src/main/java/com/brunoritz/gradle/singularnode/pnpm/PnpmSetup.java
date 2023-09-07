package com.brunoritz.gradle.singularnode.pnpm;

import com.brunoritz.gradle.singularnode.NodeJsExtension;
import com.brunoritz.gradle.singularnode.nodejs.InstallNodeJsTask;
import com.brunoritz.gradle.singularnode.platform.Lookup;
import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayout;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import static com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory.platformDependentLayout;

/**
 * Configures all required tasks and properties for the PNPM package manager.
 */
public final class PnpmSetup
{
	private static final String GROUP = "PNPM";

	private PnpmSetup()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Configures the tasks needed to install the PNPM package manager on the root project. The setup task itself will
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
		TaskProvider<InstallPnpmTask> pnpmInstallationTask =
			project.getTasks().register("installPnpm", InstallPnpmTask.class);

		pnpmInstallationTask.configure(task -> {
			task.setGroup(GROUP);
			task.dependsOn(nodeInstallationTask);
			task.getInstallationLayout().set(layout);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getPnpmInstallDirectory().set(layout.pnpmInstallDirectory());
			task.getPnpmVersion().set(configuration.pnpmVersion);
		});
	}

	/**
	 * Configures the tasks for any subproject using this plugin. A task for installing packages via PNPM will be
	 * registered {@code installPnpmPackages}. Any consumer defined task of type {@link PnpmTask} will automatically
	 * be made dependent on the {@code installPnpmPackages} task.
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
		TaskProvider<Task> pnpmSetupTask = Lookup.rootProjectTask(project, "installPnpm")
			.getOrElseThrow(() -> new IllegalStateException("Requested task does not exist on root project"));
		TaskProvider<InstallPnpmPackagesTask> installPnpmPackagesTask =
			project.getTasks().register("installPnpmPackages", InstallPnpmPackagesTask.class);

		installPnpmPackagesTask.configure(task -> {
			task.setGroup(GROUP);
			task.dependsOn(pnpmSetupTask);

			task.getArgs().set(configuration.pnpmInstallArgs);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getInstallationLayout().set(layout);
		});

		project.getTasks().whenTaskAdded(newTask -> {
			if (newTask instanceof PnpmTask newPnpmTask) {
				newPnpmTask.dependsOn(installPnpmPackagesTask);
				newPnpmTask.getWorkingDirectory().set(project.getProjectDir());
				newPnpmTask.getInstallationLayout().set(layout);
			}
		});

		project.getExtensions().getExtraProperties().set("PnpmTask", PnpmTask.class);
	}
}
