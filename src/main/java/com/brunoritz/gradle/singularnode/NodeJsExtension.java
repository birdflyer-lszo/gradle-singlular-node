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
	 * The version of NodeJS to be installed. If not defined, NodeJS will not be installed and trying to call the
	 * NodeJS installation task will fail.
	 */
	public final Property<CharSequence> nodeVersion;

	/**
	 * The URL from which to download NodeJS. Subdirectories will be computed via an Ivy repository (and its dependency
	 * pattern).
	 * <p>
	 * Defaults to {@code https://nodejs.org/dist}.
	 */
	public final Property<CharSequence> nodeDownloadBase;

	/**
	 * The version of NPM to be installed. If not defined, NPM will not be installed and trying to call the NPM
	 * installation task will fail.
	 */
	public final Property<CharSequence> npmVersion;

	/**
	 * Arguments to pass to NPM when installing packages.
	 */
	public final ListProperty<CharSequence> npmInstallArgs;

	/**
	 * The version of Yarn to be installed. If not defined, Yarn will not be installed and trying to call the Yarn
	 * installation task will fail.
	 */
	public final Property<CharSequence> yarnVersion;

	/**
	 * Arguments to pass to Yarn when installing packages.
	 */
	public final ListProperty<CharSequence> yarnInstallArgs;

	/**
	 * The version of PNPM to be installed. If not defined, PNPM will not be installed and trying to call the PNPM
	 * installation task will fail.
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

		nodeDownloadBase = project.getObjects().property(CharSequence.class);
		nodeDownloadBase.convention("https://nodejs.org/dist");

		nodeVersion = project.getObjects().property(CharSequence.class);

		npmVersion = project.getObjects().property(CharSequence.class);
		npmInstallArgs = project.getObjects().listProperty(CharSequence.class);

		yarnVersion = project.getObjects().property(CharSequence.class);
		yarnInstallArgs = project.getObjects().listProperty(CharSequence.class);

		pnpmVersion = project.getObjects().property(CharSequence.class);
		pnpmInstallArgs = project.getObjects().listProperty(CharSequence.class);

		installBaseDir = project.getObjects().directoryProperty();
		installBaseDir.set(defaultInstallDir);
	}
}
