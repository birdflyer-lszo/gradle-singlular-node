package com.brunoritz.gradle.singularnode;

import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayout;

import javax.inject.Inject;
import java.io.File;

/**
 * Provides information about the managed tooling installation to the build processs.
 */
public class ManagedNodeJs
{
	/**
	 * The path to the installed NodeJS executable. The path varies depending on the operating system being used.
	 */
	public final File nodeJsExecutable;

	/**
	 * The path to the managed NPM script. The file being pointed at is the CLI script of PNPM.
	 */
	public final File npmScript;

	/**
	 * The path to the managed Yarn script. The file being pointed at is the CLI script of Yarn.
	 */
	public final File yarnScript;

	/**
	 * The path to the managed PNPM script. The file being pointed at is the CLI script of PNPM.
	 */
	public final File pnpmScript;

	/**
	 * The direcotry in which the NodeJS executable binary is stored.
	 */
	public final File nodeJsBinDir;

	@Inject
	public ManagedNodeJs(InstallationLayout layout)
	{
		nodeJsExecutable = layout.pathOfNodeExecutable();
		npmScript = layout.pathOfManagedNpmScript();
		yarnScript = layout.pathOfManagedYarnScript();
		pnpmScript = layout.pathOfManagedPnpmScript();
		nodeJsBinDir = layout.nodeJsBinDirectory();
	}
}
