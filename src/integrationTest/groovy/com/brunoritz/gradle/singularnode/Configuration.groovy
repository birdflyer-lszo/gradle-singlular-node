package com.brunoritz.gradle.singularnode

import org.gradle.api.Project

final class Configuration
{
	private Configuration() {
		throw new UnsupportedOperationException()
	}

	static void configureNodeJs(Project rootProject, @DelegatesTo(NodeJsExtension) Closure action)
	{
		def configuration = rootProject.extensions.findByType(NodeJsExtension)

		action.rehydrate(configuration, this, this).call()
	}
}
