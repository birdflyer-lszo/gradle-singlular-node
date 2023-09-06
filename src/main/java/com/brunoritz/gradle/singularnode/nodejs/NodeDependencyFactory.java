package com.brunoritz.gradle.singularnode.nodejs;

import io.vavr.control.Option;
import io.vavr.control.Try;

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

		return operatingSystem.map(osName ->
			String.format("org.nodejs:node:%s:%s-%s@%s",
				version,
				osName,
				computeArchitecture(systemProperties, version),
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

	private static String computeArchitecture(Properties systemProperties, CharSequence version)
	{
		String osArchitecture = systemProperties.getProperty("os.arch", "").toLowerCase(Locale.ENGLISH);
		Option<Integer> majorVersion = extractMajorVersion(version);

		if (majorVersion.isDefined() && (majorVersion.get() >= 16) && "aarch64".equals(osArchitecture)) {
			return "arm64";
		} else {
			return "x64";
		}
	}

	private static Option<Integer> extractMajorVersion(CharSequence version)
	{
		String[] parts = version.toString().split("\\.");

		return Try.of(() -> Integer.parseInt(parts[0]))
			.map(Option::of)
			.getOrElse(Option.none());
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
