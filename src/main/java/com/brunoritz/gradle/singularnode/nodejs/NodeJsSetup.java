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

public final class NodeJsSetup
{
	private NodeJsSetup()
	{
		throw new UnsupportedOperationException();
	}

	public static TaskProvider<InstallNodeJsTask> configureNodeJsInstallation(Project project)
	{
		TaskProvider<InstallNodeJsTask> nodeInstallationTask = registerInstallTask(project);

		project.afterEvaluate(evauated -> createNodeDependency(evauated, nodeInstallationTask));
		configureNodeRepository(project);

		return nodeInstallationTask;
	}

	private static TaskProvider<InstallNodeJsTask> registerInstallTask(Project project)
	{
		NodeJsExtension configuration = project.getExtensions().create("nodeJs", NodeJsExtension.class);
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

	private static void configureNodeRepository(Project project)
	{
		project.getRepositories().ivy(repo -> {
			repo.setName("nodejs");
			repo.setUrl("https://nodejs.org/dist");
			repo.patternLayout(layout -> layout.artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]"));
			repo.metadataSources(IvyArtifactRepository.MetadataSources::artifact);
			repo.content(content -> content.includeModule("org.nodejs", "node"));
		});
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
