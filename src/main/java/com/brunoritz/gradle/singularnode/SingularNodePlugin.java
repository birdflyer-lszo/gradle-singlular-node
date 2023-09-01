package com.brunoritz.gradle.singularnode;

import com.brunoritz.gradle.singularnode.npm.NpmSetup;
import com.brunoritz.gradle.singularnode.npm.NpmTask;
import com.brunoritz.gradle.singularnode.nodejs.InstallNodeJsTask;
import com.brunoritz.gradle.singularnode.platform.Lookup;
import com.brunoritz.gradle.singularnode.nodejs.NodeJsSetup;
import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayout;
import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory;
import com.brunoritz.gradle.singularnode.pnpm.PnpmSetup;
import com.brunoritz.gradle.singularnode.pnpm.PnpmTask;
import com.brunoritz.gradle.singularnode.yarn.YarnSetup;
import com.brunoritz.gradle.singularnode.yarn.YarnTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

/**
 * The {@code Singular Node Installation} plugin provides a single NodeJS/NPM/Yarn/PNPM installation throughout the
 * entire project. Its intention is to reduce build time and complexity by only installing the tooling once and then
 * allow any subproject to consume it. It further reduces complexity by forcing the project to use a single version
 * of the tooling.
 * <p>
 * This plugin provides a {@code nodeJs} extension through which the aspects of the tooling setup con be configured. The
 * extension is only available on the root project.
 * <p>
 * In order to simplify authoring of custom NPM/Yarn/PNPM task, the {@link NpmTask}, {@link YarnTask} and
 * {@link PnpmTask} types are made available to the project via the extra properties {@code NpmTask}, {@code YarnTask}
 * and {@code PnpmTask}. That eliminates the need to {@code import} the task type. With these properties in place, task
 * authors can simply create new tasks using
 * <pre>
 * task customNpmTask(type: NpmTask) {
 *     // ...
 * }
 * task customYarnTask(type: YarnTask) {
 *     // ...
 * }
 * task customPnpmTask(type: PnpmTask) {
 *     // ...
 * }
 * </pre>
 * <p>
 * NodeJS is downloaded as a Gradle dependency. By default, {@code https://nodejs.org/dist} is used as distribution
 * base.
 * <p>
 * Further details on the behavior can be found in the documentation of the tasks and the extension.
 *
 * @see NodeJsExtension
 * @see NpmTask
 * @see YarnTask
 * @see PnpmTask
 */
public class SingularNodePlugin
	implements Plugin<Project>
{
	@Override
	public void apply(Project project)
	{
		if (project.getRootProject().equals(project)) {
			configureRootProject(project);
		} else {
			configureSubproject(project);
		}
	}

	private static void configureRootProject(Project project)
	{
		TaskProvider<InstallNodeJsTask> nodeInstallationTask = NodeJsSetup.configureNodeJsInstallation(project);

		NpmSetup.setupRootTasks(project, nodeInstallationTask);
		PnpmSetup.setupRootTasks(project, nodeInstallationTask);
		YarnSetup.setupRootTasks(project, nodeInstallationTask);
	}

	private static void configureSubproject(Project project)
	{
		ensureRootProjectHasPlugin(project);

		NpmSetup.setupChildTasks(project);
		PnpmSetup.setupChildTasks(project);
		YarnSetup.setupChildTasks(project);

		publishNodeInstallationInfo(project);
	}

	private static void ensureRootProjectHasPlugin(Project project)
	{
		if (!project.getRootProject().getPlugins().hasPlugin("com.brunoritz.gradle.singular-node")) {
			throw new IllegalStateException("Plugin must be applied to the root project");
		}
	}

	private static void publishNodeInstallationInfo(Project project)
	{
		NodeJsExtension configuration = Lookup.pluginConfiguration(project)
			.getOrElseThrow(() -> new IllegalStateException("Plugin configuration does not exist in root project"));
		InstallationLayout layout = InstallationLayoutFactory.platformDependentLayout(configuration.installBaseDir)
			.getOrElseThrow(() -> new IllegalStateException("Running on unsupported OS"));

		project.getExtensions().create("managedNodeJs", ManagedNodeJs.class, layout);
	}
}
