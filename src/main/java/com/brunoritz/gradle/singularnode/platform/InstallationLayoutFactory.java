package com.brunoritz.gradle.singularnode.platform;

import io.vavr.control.Option;
import org.gradle.api.file.DirectoryProperty;

import java.util.Locale;

/**
 * A factory that produces environment dependent installation layouts.
 */
public final class InstallationLayoutFactory
{
	private InstallationLayoutFactory()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a new installation layout starting at the given installation base directoy. The layout represented
	 * depends on the execution platforn. Any NodeJS and Yarn installations are to be made under {@code
	 * installBaseDir}.
	 *
	 * @param installBaseDir
	 * 	The parent directory of all tooling managed by this plugin
	 */
	public static Option<InstallationLayout> platformDependentLayout(DirectoryProperty installBaseDir)
	{
		String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

		if (osName.contains("mac os") || osName.contains("linux")) {
			return Option.of(new UnixInstallationLayout(installBaseDir));
		} else if (osName.contains("windows")) {
			return Option.of(new WindowsInstallationLayout(installBaseDir));
		} else {
			return Option.none();
		}
	}
}
