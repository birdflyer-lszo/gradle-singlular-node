package com.brunoritz.gradle.singularnode.platform.layout;

import java.io.File;

/**
 * A convenience utility to assemble {@code File}s representing a path consisting of multiple parts.
 */
final class PathCombination
{
	private PathCombination()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Constructs a file representing the path of the given arguments in order. Each argument represents one single
	 * directory or file without any path separator.
	 *
	 * @param top
	 * 	The first part of the path to assemble
	 * @param parts
	 * 	The additional individual components of the path to assemble
	 *
	 * @return The combined path
	 */
	static File combine(File top, String... parts)
	{
		File result = top;

		for (String part : parts) {
			result = new File(result, part);
		}

		return result;
	}
}
