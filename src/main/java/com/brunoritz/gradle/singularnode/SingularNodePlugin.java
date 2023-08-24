package com.brunoritz.gradle.singularnode;

import com.brunoritz.gradle.singularnode.execute.InstallNpmPackagesTask;
import com.brunoritz.gradle.singularnode.execute.InstallPnpmPackagesTask;
import com.brunoritz.gradle.singularnode.execute.InstallYarnPackagesTask;
import com.brunoritz.gradle.singularnode.execute.NpmTask;
import com.brunoritz.gradle.singularnode.execute.PnpmTask;
import com.brunoritz.gradle.singularnode.execute.YarnTask;
import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayout;
import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory;
import com.brunoritz.gradle.singularnode.platform.NodeDependencyFactory;
import com.brunoritz.gradle.singularnode.setup.InstallNodeJsTask;
import com.brunoritz.gradle.singularnode.setup.InstallNpmTask;
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

import static com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory.platformDependentLayout;

import java.io.File;

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
		TaskProvider<InstallNpmTask> npmInstallationTask = tasks.register("installNpm", InstallNpmTask.class);
		TaskProvider<InstallYarnTask> yarnInstallationTask = tasks.register("installYarn", InstallYarnTask.class);
		TaskProvider<InstallPnpmTask> pnpmInstallationTask = tasks.register("installPnpm", InstallPnpmTask.class);

		nodeInstallationTask.configure(task -> {
			task.setGroup(SINGULAR_NODE_GROUP);
			task.getInstallationLayout().set(layout);
			task.getNodeJsInstallDir().set(layout.nodeJsInstallDir());
		});

		npmInstallationTask.configure(task -> {
			task.setGroup(SINGULAR_NODE_GROUP);
			task.dependsOn(nodeInstallationTask);
			task.getInstallationLayout().set(layout);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getNpmInstallDirectory().set(layout.npmInstallDirectory());
			task.getNpmVersion().set(configuration.npmVersion);
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
		TaskProvider<Task> npmSetupTask = rootProjectTask(project, "installNpm");
		TaskProvider<Task> yarnSetupTask = rootProjectTask(project, "installYarn");
		TaskProvider<Task> pnpmSetupTask = rootProjectTask(project, "installPnpm");
		TaskProvider<InstallNpmPackagesTask> installNpmPackagesTask =
			project.getTasks().register("installNpmPackages", InstallNpmPackagesTask.class);
		TaskProvider<InstallYarnPackagesTask> installYarnPackagesTask =
			project.getTasks().register("installYarnPackages", InstallYarnPackagesTask.class);
		TaskProvider<InstallPnpmPackagesTask> installPnpmPackagesTask =
			project.getTasks().register("installPnpmPackages", InstallPnpmPackagesTask.class);
		InstallationLayout layout = platformDependentLayout(configuration.installBaseDir)
			.getOrElseThrow(() -> new IllegalStateException("Running on unsupported OS"));

		installNpmPackagesTask.configure(task -> {
			task.setGroup(SINGULAR_NODE_GROUP);
			task.dependsOn(npmSetupTask);

			task.getArgs().set(configuration.npmInstallArgs);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getInstallationLayout().set(layout);
		});

		installYarnPackagesTask.configure(task -> {
			task.setGroup(SINGULAR_NODE_GROUP);
			task.dependsOn(yarnSetupTask);

			task.getArgs().set(configuration.yarnInstallArgs);
			task.getWorkingDirectory().set(project.getProjectDir());
			task.getInstallationLayout().set(layout);
		});

		installPnpmPackagesTask.configure(task -> {
			task.setGroup(SINGULAR_NODE_GROUP);
			task.dependsOn(pnpmSetupTask);

			task.getArgs().set(configuration.pnpmInstallArgs);
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

			if (newTask instanceof YarnTask newYarnTask) {
				newYarnTask.dependsOn(installYarnPackagesTask);
				newYarnTask.getWorkingDirectory().set(project.getProjectDir());
				newYarnTask.getInstallBaseDir().set(configuration.installBaseDir);
				newYarnTask.getInstallationLayout().set(layout);
			}

			if (newTask instanceof PnpmTask newPnpmTask) {
				newPnpmTask.dependsOn(installPnpmPackagesTask);
				newPnpmTask.getWorkingDirectory().set(project.getProjectDir());
				newPnpmTask.getInstallBaseDir().set(configuration.installBaseDir);
				newPnpmTask.getInstallationLayout().set(layout);
			}
		});

		project.getExtensions().getExtraProperties().set("NpmTask", NpmTask.class);
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
