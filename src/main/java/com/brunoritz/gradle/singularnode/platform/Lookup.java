package com.brunoritz.gradle.singularnode.platform;

import com.brunoritz.gradle.singularnode.NodeJsExtension;
import io.vavr.control.Option;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.tasks.TaskProvider;

/**
 * Utility methods to lookup configuration and tasks related to the NodeJS plugin.
 */
public class Lookup
{
	/**
	 * Returns the configuration extension for this plugin. If the root project does not have this plugin applied,
	 * {@code none} will be returned.
	 *
	 * @param project
	 * 	The project via which the root project will be accessed
	 *
	 * @return The configuration extension or {@code none}
	 */
	public static Option<NodeJsExtension> pluginConfiguration(Project project)
	{
		return Option.of(project.getRootProject().getExtensions().findByType(NodeJsExtension.class));
	}

	/**
	 * Returns an task from the root project by its name. If the given task does not exist on the root project,
	 * {@code none} will be returned.
	 *
	 * @param project
	 * 	The project via which the root project will be accessed
	 * @param name
	 * 	The name of the task to return
	 *
	 * @return The requested task or {@code none}
	 */
	public static Option<TaskProvider<Task>> rootProjectTask(Project project, String name)
	{
		try {
			return Option.of(project.getRootProject().getTasks().named(name));
		} catch (UnknownTaskException e) {
			return Option.none();
		}
	}
}
