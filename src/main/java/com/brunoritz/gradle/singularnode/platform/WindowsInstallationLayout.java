package com.brunoritz.gradle.singularnode.platform;

import org.gradle.api.file.DirectoryProperty;

import static com.brunoritz.gradle.singularnode.platform.PathFactory.combine;

import java.io.File;

/**
 * The installation layout for Windows systems. On Windows, most commands either contain a {@code .exe} or {@code .cmd}
 * extension and are sometimes located in slightly different directories.
 */
public class WindowsInstallationLayout
	implements InstallationLayout
{
	private final DirectoryProperty installBaseDir;

	WindowsInstallationLayout(DirectoryProperty installBaseDir)
	{
		this.installBaseDir = installBaseDir;
	}

	@Override
	public File nodeJsInstallDir()
	{
		return installBaseDir.dir("node").get().getAsFile();
	}

	@Override
	public File yarnInstallDirectory()
	{
		return installBaseDir.dir("yarn").get().getAsFile();
	}

	@Override
	public File pnpmInstallDirectory()
	{
		return installBaseDir.dir("pnpm").get().getAsFile();
	}

	@Override
	public File nodeJsBinDirectory()
	{
		return nodeJsInstallDir();
	}

	@Override
	public File pathOfNodeExecutable()
	{
		return combine(nodeJsBinDirectory(), "node.exe");
	}

	@Override
	public File pathOfBundledNpmScript()
	{
		return pathOfBundledCliScript("npm");
	}

	@Override
	public File pathOfBundnledNpxScript()
	{
		return pathOfBundledCliScript("npx");
	}

	@Override
	public File pathOfBundledCliScript(String scriptName)
	{
		String cliScript = String.format("%s-cli.js", scriptName);

		return combine(nodeJsInstallDir(), "node_modules", "npm", "bin", cliScript);
	}

	@Override
	public File pathOfManagedYarnScript()
	{
		return combine(yarnInstallDirectory(), "node_modules", "yarn", "bin", "yarn.js");
	}

	@Override
	public File pathOfManagedPnpmScript()
	{
		return combine(pnpmInstallDirectory(), "node_modules", "pnpm", "bin", "pnpm.cjs");
	}
}
