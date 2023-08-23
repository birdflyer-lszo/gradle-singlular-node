package com.brunoritz.gradle.singularnode.platform.layout;

import static com.brunoritz.gradle.singularnode.platform.layout.PathFactory.combine;

import org.gradle.api.file.DirectoryProperty;

import java.io.File;

/**
 * The installation layout for Windows systems. On Windows, most commands either contain a {@code .exe} or {@code .cmd}
 * extension and are sometimes located in slightly different directories.
 */
public class WindowsInstallationLayout
	extends InstallationLayout
{
	WindowsInstallationLayout(DirectoryProperty installBaseDir)
	{
		super(installBaseDir);
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
