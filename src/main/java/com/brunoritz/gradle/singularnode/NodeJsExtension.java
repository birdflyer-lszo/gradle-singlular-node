package com.brunoritz.gradle.singularnode;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Allows the root project to configure the details of the tooling to be installed.
 */
public class NodeJsExtension
{
	/**
	 * The version of NodeJS to be installed.
	 * <p>
	 * <b>This is a mandatory property.</b>
	 */
	public final Property<CharSequence> nodeVersion;

	/**
	 * The version of Yarn to be installed.
	 * <p>
	 * <b>This is a mandatory property.</b>
	 */
	public final Property<CharSequence> yarnVersion;

	/**
	 * Arguments to pass to Yarn when installing packages.
	 */
	public final ListProperty<CharSequence> yarnInstallArgs;

	/**
	 * The version of PNPM to be installed.
	 * <p>
	 * <b>This is a mandatory property.</b>
	 */
	public final Property<CharSequence> pnpmVersion;

	/**
	 * Arguments to pass to PNPM when installing packages.
	 */
	public final ListProperty<CharSequence> pnpmInstallArgs;

	/**
	 * The directory into wich to install NodeJS and Yarn.
	 * <p>
	 * Defaults to {@code {$rootProjectDir}/nodejs}
	 */
	public final DirectoryProperty installBaseDir;

	@Inject
	public NodeJsExtension(Project project)
	{
		Directory defaultInstallDir = project.getLayout().getProjectDirectory().dir("nodejs");

		nodeVersion = project.getObjects().property(CharSequence.class);
		yarnVersion = project.getObjects().property(CharSequence.class);
		yarnInstallArgs = project.getObjects().listProperty(CharSequence.class);
		pnpmVersion = project.getObjects().property(CharSequence.class);
		pnpmInstallArgs = project.getObjects().listProperty(CharSequence.class);

		installBaseDir = project.getObjects().directoryProperty();
		installBaseDir.set(defaultInstallDir);
	}
}
