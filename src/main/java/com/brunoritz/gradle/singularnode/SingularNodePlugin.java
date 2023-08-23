package com.brunoritz.gradle.singularnode;

import com.brunoritz.gradle.singularnode.execute.InstallPnpmPackagesTask;
import com.brunoritz.gradle.singularnode.execute.InstallYarnPackagesTask;
import com.brunoritz.gradle.singularnode.execute.PnpmTask;
import com.brunoritz.gradle.singularnode.execute.YarnTask;
import com.brunoritz.gradle.singularnode.platform.InstallationLayout;
import com.brunoritz.gradle.singularnode.platform.InstallationLayoutFactory;
import com.brunoritz.gradle.singularnode.platform.NodeDependencyFactory;
import com.brunoritz.gradle.singularnode.setup.InstallNodeJsTask;
import com.brunoritz.gradle.singularnode.setup.InstallPnpmTask;
import com.brunoritz.gradle.singularnode.setup.InstallYarnTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository.MetadataSources;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import static com.brunoritz.gradle.singularnode.platform.InstallationLayoutFactory.platformDependentLayout;

import java.io.File;

/**
 * The {@code Singular Node Installation} plugin provides a single NodeJS/Yarn/PNPM installation throughout the entire
 * project. Its intention is to reduce build time and complexity by only installing the tooling once and then allow any
 * subproject to consume it. It further reduces complexity by forcing the project to use a single version of the
 * tooling.
 * <p>
 * This plugin provides a {@code nodeJs} extension through which the aspects of the tooling setup con be configured. The
 * extension is only available on the root project.
 * <p>
 * In order to simplify authoring of custom Yarn/PNPM task, the {@link YarnTask} and  {@link PnpmTask} types are made
 * available to the project via the extra properties {@code YarnTask} and  {@code PnpmTask}. That eliminates the need to
 * {@code import} the task type. With these properties in place, task authors can simply create new tasks using
 * <pre>
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
 * @see YarnTask
 * @see PnpmTask
 */
public class SingularNodePlugin
	implements Plugin<Project>
{
	private static final String SINGULAR_NODE_GROUP = "Singular Node Installation";

	@Override
	public void apply(Project project)
	{
		if (project.getRootProject().equals(project)) {
			addRepository(project);
			createInstallationTasks(project);
		} else {
			ensureRootProjectHasPlugin(project);
			createConsumerTasks(project);
			publishNodeInstallationInfo(project);
		}
	}

	private static void addRepository(Project project)
	{
		project.getRepositories().ivy(repo -> {
			repo.setName("nodejs");
			repo.setUrl("https://nodejs.org/dist");
			repo.patternLayout(layout -> layout.artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]"));
			repo.metadataSources(MetadataSources::artifact);
			repo.content(content -> content.includeModule("org.nodejs", "node"));
		});
	}

	private static void createInstallationTasks(Project project)
	{
		TaskContainer tasks = project.getTasks();
		NodeJsExtension configuration = project.getExtensions().create("nodeJs", NodeJsExtension.class);
		InstallationLayout layout = platformDependentLayout(configuration.installBaseDir)
			.getOrElseThrow(() -> new IllegalStateException("Running on unsupported OS"));

		TaskProvider<InstallNodeJsTask> nodeInstallationTask =
			tasks.register("installNodeJs", InstallNodeJsTask.class);
		TaskProvider<InstallYarnTask> yarnInstallationTask = tasks.register("installYarn", InstallYarnTask.class);
		TaskProvider<InstallPnpmTask> pnpmInstallationTask = tasks.register("installPnpm", InstallPnpmTask.class);

		nodeInstallationTask.configure(task -> {
			task.setGroup(SINGULAR_NODE_GROUP);
			task.getInstallationLayout().set(layout);
			task.getNodeJsInstallDir().set(layout.nodeJsInstallDir());
		});

		yarnInstallationTask.configure(task -> {
			task.setGroup(SINGULAR_NODE_GROUP);
			task.dependsOn(nodeInstallationTask);
			task.getInstallationLayout().set(layout);
			task.getInstallationLayout().set(layout);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getYarnInstallDirectory().set(layout.yarnInstallDirectory());
			task.getYarnVersion().set(configuration.yarnVersion);
		});

		pnpmInstallationTask.configure(task -> {
			task.setGroup(SINGULAR_NODE_GROUP);
			task.dependsOn(nodeInstallationTask);
			task.getInstallationLayout().set(layout);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getPnpmInstallDirectory().set(layout.pnpmInstallDirectory());
			task.getPnpmVersion().set(configuration.pnpmVersion);
		});

		project.afterEvaluate(evauated -> createNodeDependency(evauated, nodeInstallationTask));
	}

	private void ensureRootProjectHasPlugin(Project project)
	{
		if (!project.getRootProject().getPlugins().hasPlugin("com.brunoritz.gradle.singular-node")) {
			throw new IllegalStateException("Plugin must be applied to the root project");
		}
	}

	private void createConsumerTasks(Project project)
	{
		NodeJsExtension configuration = pluginConfiguration(project);
		TaskProvider<Task> yarnSetupTask = rootProjectTask(project, "installYarn");
		TaskProvider<Task> pnpmSetupTask = rootProjectTask(project, "installPnpm");
		TaskProvider<InstallYarnPackagesTask> installYarnPackagesPackagesTask =
			project.getTasks().register("installYarnPackages", InstallYarnPackagesTask.class);
		TaskProvider<InstallPnpmPackagesTask> installPnpmPackagesPackagesTask =
			project.getTasks().register("installPnpmPackages", InstallPnpmPackagesTask.class);
		InstallationLayout layout = platformDependentLayout(configuration.installBaseDir)
			.getOrElseThrow(() -> new IllegalStateException("Running on unsupported OS"));

		installYarnPackagesPackagesTask.configure(task -> {
			task.setGroup(SINGULAR_NODE_GROUP);
			task.dependsOn(yarnSetupTask);

			task.getArgs().set(configuration.yarnInstallArgs);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getInstallationLayout().set(layout);
		});

		installPnpmPackagesPackagesTask.configure(task -> {
			task.setGroup(SINGULAR_NODE_GROUP);
			task.dependsOn(pnpmSetupTask);

			task.getArgs().set(configuration.pnpmInstallArgs);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getInstallationLayout().set(layout);
		});

		project.getTasks().whenTaskAdded(newTask -> {
			if (newTask instanceof YarnTask newYarnTask) {
				newYarnTask.dependsOn(installYarnPackagesPackagesTask);
				newYarnTask.getWorkingDirectory().set(project.getProjectDir());
				newYarnTask.getInstallBaseDir().set(configuration.installBaseDir);
				newYarnTask.getInstallationLayout().set(layout);
			}

			if (newTask instanceof PnpmTask newPnpmTask) {
				newPnpmTask.dependsOn(installPnpmPackagesPackagesTask);
				newPnpmTask.getWorkingDirectory().set(project.getProjectDir());
				newPnpmTask.getInstallBaseDir().set(configuration.installBaseDir);
				newPnpmTask.getInstallationLayout().set(layout);
			}
		});

		project.getExtensions().getExtraProperties().set("YarnTask", YarnTask.class);
		project.getExtensions().getExtraProperties().set("PnpmTask", PnpmTask.class);
	}

	private void publishNodeInstallationInfo(Project project)
	{
		NodeJsExtension configuration = pluginConfiguration(project);
		InstallationLayout layout = InstallationLayoutFactory.platformDependentLayout(configuration.installBaseDir)
			.getOrElseThrow(() -> new IllegalStateException("Running on unsupported OS"));

		project.getExtensions().create("managedNodeJs", ManagedNodeJs.class, layout);
	}

	private static TaskProvider<Task> rootProjectTask(Project project, String name)
	{
		return project.getRootProject().getTasks().named(name);
	}

	private static NodeJsExtension pluginConfiguration(Project project)
	{
		NodeJsExtension configuration = project.getRootProject().getExtensions().findByType(NodeJsExtension.class);

		if (configuration == null) {
			throw new IllegalStateException("Plugin configuration does not exist in root project");
		}

		return configuration;
	}

	private static void createNodeDependency(Project project, TaskProvider<InstallNodeJsTask> nodeInstallationTask)
	{
		NodeJsExtension configuration = project.getExtensions().getByType(NodeJsExtension.class);
		String nodeDependencySpec = NodeDependencyFactory.computeDependencyString(
				configuration.nodeVersion.get(),
				System.getProperties()
			)
			.getOrElseThrow(() -> new IllegalStateException("Running on unsupported operating system"));
		Dependency nodeDependency = project.getDependencies().create(nodeDependencySpec);
		Provider<File> archiveProvider = project.getProviders().provider(() ->
			project.getConfigurations().detachedConfiguration(nodeDependency)
				.setTransitive(false)
				.resolve()
				.iterator()
				.next()
		);

		nodeInstallationTask.configure(task -> task.getNodeArchive().set(project.getLayout().file(archiveProvider)));
	}
}
