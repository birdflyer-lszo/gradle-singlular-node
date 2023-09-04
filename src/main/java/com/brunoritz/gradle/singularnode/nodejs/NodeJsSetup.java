package com.brunoritz.gradle.singularnode.nodejs;

import com.brunoritz.gradle.singularnode.NodeJsExtension;
import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayout;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import static com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory.platformDependentLayout;

import java.io.File;

/**
 * Configures all tasks, repositories and dependencies required for installing NodeJS locally.
 */
public final class NodeJsSetup
{
	private NodeJsSetup()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Configures the project to support installing NodeJS. In particular, this method
	 * <ul>
	 *     <li>Creates a {@code nodeJs} extension via which the installation can be customized</li>
	 *     <li>Creates an Ivy repository that is used to fetch the NodeJS archive from</li>
	 *     <li>Creates a depdency configuration for the specific NdeJS version</li>
	 *     <li>Creates an {@code installNodeJs} task that performs the actual installation</li>
	 * </ul>
	 *
	 * @param project
	 * 	The project that shall provide installation support (normally the root project)
	 *
	 * @return The task that performs the actual installation
	 */
	public static TaskProvider<InstallNodeJsTask> configureNodeJsInstallation(Project project)
	{
		NodeJsExtension configuration = project.getExtensions().create("nodeJs", NodeJsExtension.class);
		TaskProvider<InstallNodeJsTask> nodeInstallationTask = registerInstallTask(project, configuration);

		configureNodeRepository(project, configuration);
		createNodeDependency(project, nodeInstallationTask);

		return nodeInstallationTask;
	}

	private static TaskProvider<InstallNodeJsTask> registerInstallTask(Project project, NodeJsExtension configuration)
	{
		InstallationLayout layout = platformDependentLayout(configuration.installBaseDir)
			.getOrElseThrow(() -> new IllegalStateException("Running on unsupported OS"));
		TaskProvider<InstallNodeJsTask> nodeInstallationTask =
			project.getTasks().register("installNodeJs", InstallNodeJsTask.class);

		nodeInstallationTask.configure(task -> {
			task.setGroup("NodeJS");
			task.getInstallationLayout().set(layout);
			task.getNodeJsInstallDir().set(layout.nodeJsInstallDir());
		});

		return nodeInstallationTask;
	}

	private static void configureNodeRepository(Project project, NodeJsExtension configuration)
	{
		project.getRepositories().ivy(repo -> {
			repo.setName("com.brunoritz.gradle.singularnode.nodeJsIvy");
			repo.setUrl(configuration.nodeDownloadBase);
			repo.patternLayout(layout -> layout.artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]"));
			repo.metadataSources(IvyArtifactRepository.MetadataSources::artifact);
			repo.content(content -> content.includeModule("org.nodejs", "node"));
		});
	}

	private static void createNodeDependency(Project project, TaskProvider<InstallNodeJsTask> nodeInstallationTask)
	{
		Provider<File> archiveProvider = project.getProviders().provider(() -> nodeJsArchive(project));

		nodeInstallationTask.configure(task -> task.getNodeArchive().set(project.getLayout().file(archiveProvider)));
	}

	private static File nodeJsArchive(Project project)
	{
		NodeJsExtension configuration = project.getExtensions().getByType(NodeJsExtension.class);
		String nodeDependencySpec = NodeDependencyFactory.computeDependencyString(
				configuration.nodeVersion.get(),
				System.getProperties()
			)
			.getOrElseThrow(() -> new IllegalStateException("Running on unsupported operating system"));
		Dependency nodeDependency = project.getDependencies().create(nodeDependencySpec);

		return project.getConfigurations().detachedConfiguration(nodeDependency)
			.setTransitive(false)
			.resolve()
			.iterator()
			.next();
	}
}
