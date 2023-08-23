package com.brunoritz.gradle.singularnode.platform;

import org.gradle.api.file.DirectoryProperty;

import static com.brunoritz.gradle.singularnode.platform.PathFactory.combine;

import java.io.File;

/**
 * The installation layout for Unix and compabible systems (macOs, Linux).
 */
public class UnixInstallationLayout
	implements InstallationLayout
{
	private final DirectoryProperty installBaseDir;

	UnixInstallationLayout(DirectoryProperty installBaseDir)
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
		return combine(nodeJsInstallDir(), "bin");
	}

	@Override
	public File pathOfNodeExecutable()
	{
		return combine(nodeJsBinDirectory(), "node");
	}

	@Override
	public File pathOfBundledNpmScript()
	{
		return combine(nodeJsBinDirectory(), "npm");
	}

	@Override
	public File pathOfBundnledNpxScript()
	{
		return combine(nodeJsBinDirectory(), "npx");
	}

	@Override
	public File pathOfBundledCliScript(String scriptName)
	{
		String cliScript = String.format("%s-cli.js", scriptName);

		return combine(nodeJsInstallDir(), "lib", "node_modules", "npm", "bin", cliScript);
	}

	@Override
	public File pathOfManagedYarnScript()
	{
		return combine(yarnInstallDirectory(), "bin", "yarn");
	}

	@Override
	public File pathOfManagedPnpmScript()
	{
		return combine(pnpmInstallDirectory(), "bin", "pnpm");
	}
}
