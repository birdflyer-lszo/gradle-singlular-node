package com.brunoritz.gradle.singularnode.platform.layout;

import org.gradle.api.file.DirectoryProperty;

import java.io.File;

/**
 * Represents all relevant directories and binaries needed by the plugin. Any access to a file within the tooling
 * installation should be made via this class in order to compensate for different directory layouts on various
 * platforms.
 */
public abstract class InstallationLayout
{
	private final DirectoryProperty installBaseDir;

	protected InstallationLayout(DirectoryProperty installBaseDir)
	{
		this.installBaseDir = installBaseDir;
	}

	/**
	 * Returns the directory within which NodeJS is installed. The directoy is assumed to directly contain the {@code
	 * bin}, {@code node_modules} and compantion directories without an intermediate directory representing the
	 * version of the installed tooling.
	 */
	public File nodeJsInstallDir()
	{
		return installBaseDir.dir("node").get().getAsFile();
	}

	/**
	 * Returns the directory whiin which the managed version of NPM is installed. The directory returned is the one
	 * used to install NPM into using the {@code --prefix} switch.
	 */
	public File npmInstallDirectory()
	{
		return installBaseDir.dir("npm").get().getAsFile();
	}

	/**
	 * Returns the directory whiin which the managed version of Yarn is installed. The directory returned is the one
	 * used to install Yarn into using the {@code --prefix} switch.
	 */
	public File yarnInstallDirectory()
	{
		return installBaseDir.dir("yarn").get().getAsFile();
	}

	/**
	 * Returns the directory within which the managed version of PNPM is installed. The directory returned is the one
	 * used to install PNPM into using the {@code --prefix} switch.
	 */
	public File pnpmInstallDirectory()
	{
		return installBaseDir.dir("pnpm").get().getAsFile();
	}

	/**
	 * Returns the directory containing the NodeJS executable. Not that this might not be a subdirectory of the
	 * installation directory, but just the installation directory itse.f
	 */
	public abstract File nodeJsBinDirectory();

	/**
	 * Returns the full path of the executable representing the NodeJS engine.
	 */
	public abstract File pathOfNodeExecutable();

	/**
	 * Returns the full path to the bundled NPM script. Note that this is not the managed version and should only be
	 * used for installing the desired version of NPM. The file returned is (depending on the platform) just a
	 * symlink to the actual CLI script.
	 */
	public abstract File pathOfBundledNpmScript();

	/**
	 * Returns the full path to the bundled NPX script. Note that this is not the managed version and should only be
	 * used or installing the desired version of NPX.
	 */
	public abstract File pathOfBundnledNpxScript();

	/**
	 * Returns the full path of a bundled NPM CLI script. Such files reside within the {@code node_modules} directory
	 * bundled with the NodeJS installation. The name of the script file itself is {@code <scriptName>-cli.js}.
	 *
	 * @param scriptName
	 * 	The script's name without the {@code cli-js} suffix
	 */
	public abstract File pathOfBundledCliScript(String scriptName);

	/**
	 * Returns the full path to the Yarn installation managed by this plugin. This is the verison of Yarn to be used
	 * for
	 * all Yarn invocations except for installing the managed version itself.
	 */
	public abstract File pathOfManagedYarnScript();

	/**
	 * Returns the full path to the PNPM installation managed by this plugin. This is the verison of PNPM to be used
	 * for
	 * all PNPM invocations except for installing the managed version itself.
	 */
	public abstract File pathOfManagedPnpmScript();
}
