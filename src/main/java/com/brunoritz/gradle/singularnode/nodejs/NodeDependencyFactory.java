package com.brunoritz.gradle.singularnode.nodejs;

import io.vavr.control.Option;

import java.util.Locale;
import java.util.Properties;

/**
 * A factory to compute a Gradle compatible simplified dependency string for the requested version of NodeJS. At this
 * time, only the following operating systems are recognized/supported.
 * <ul>
 *    <li>macOS</li>
 *    <li>Windows</li>
 *    <li>Linux</li>
 * </ul>
 * <p>
 * 32bit platforms are not supported.
 */
final class NodeDependencyFactory
{
	private NodeDependencyFactory()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Computes the dependency string for the requested version.
	 *
	 * @param version
	 * 	The version of NodeJS to install
	 * @param systemProperties
	 * 	The system properties containing the details of the OS and the architecture.
	 *
	 * @return The short-hand dependency string or {@code none()}, if the environment is not supported
	 */
	static Option<String> computeDependencyString(CharSequence version, Properties systemProperties)
	{
		Option<String> operatingSystem = computeOperatingSystem(systemProperties);

		// For now, only x64 support, M1 later
		return operatingSystem.map(osName ->
			String.format("org.nodejs:node:%s:%s-x64@%s",
				version,
				osName,
				computeExtension(systemProperties)
			)
		);
	}

	private static Option<String> computeOperatingSystem(Properties systemProperties)
	{
		String osName = systemProperties.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);

		if (osName.contains("windows")) {
			return Option.of("win");
		} else if (osName.contains("mac os")) {
			return Option.of("darwin");
		} else if (osName.contains("linux")) {
			return Option.of("linux");
		} else {
			return Option.none();
		}
	}

	private static String computeExtension(Properties systemProperties)
	{
		String osName = systemProperties.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);

		if (osName.contains("windows")) {
			return "zip";
		} else {
			return "tar.gz";
		}
	}
}
