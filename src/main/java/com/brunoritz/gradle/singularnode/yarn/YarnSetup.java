package com.brunoritz.gradle.singularnode.yarn;

import com.brunoritz.gradle.singularnode.NodeJsExtension;
import com.brunoritz.gradle.singularnode.platform.InstallNodeJsTask;
import com.brunoritz.gradle.singularnode.platform.Lookup;
import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayout;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import static com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory.platformDependentLayout;

/**
 * Configures all required tasks and properties for the Yarn package manager.
 */
public final class YarnSetup
{
	public static final String GROUP = "Yarn";

	private YarnSetup()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Configures the tasks needed to install the Yarn package manager on the root project. The setup task itself will
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
		TaskProvider<InstallYarnTask> yarnInstallationTask =
			project.getTasks().register("installYarn", InstallYarnTask.class);

		yarnInstallationTask.configure(task -> {
			task.setGroup(GROUP);
			task.dependsOn(nodeInstallationTask);
			task.getInstallationLayout().set(layout);
			task.getInstallationLayout().set(layout);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getYarnInstallDirectory().set(layout.yarnInstallDirectory());
			task.getYarnVersion().set(configuration.yarnVersion);
		});
	}

	/**
	 * Configures the tasks for any subproject using this plugin. A task for installing packages via Yarn will be
	 * registered {@code installYarnPackages}. Any consumer defined task of type {@link YarnTask} will automatically
	 * be made dependent on the {@code installYarnPackages} task.
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
		TaskProvider<Task> yarnSetupTask = Lookup.rootProjectTask(project, "installYarn")
			.getOrElseThrow(() -> new IllegalStateException("Requested task does not exist on root project"));
		TaskProvider<InstallYarnPackagesTask> installYarnPackagesTask =
			project.getTasks().register("installYarnPackages", InstallYarnPackagesTask.class);

		installYarnPackagesTask.configure(task -> {
			task.setGroup(GROUP);
			task.dependsOn(yarnSetupTask);

			task.getArgs().set(configuration.yarnInstallArgs);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getInstallationLayout().set(layout);
		});

		project.getTasks().whenTaskAdded(newTask -> {
			if (newTask instanceof YarnTask newYarnTask) {
				newYarnTask.dependsOn(installYarnPackagesTask);
				newYarnTask.getWorkingDirectory().set(project.getProjectDir());
				newYarnTask.getInstallBaseDir().set(configuration.installBaseDir);
				newYarnTask.getInstallationLayout().set(layout);
			}
		});

		project.getExtensions().getExtraProperties().set("YarnTask", YarnTask.class);
	}
}
