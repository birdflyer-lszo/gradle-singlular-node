package com.brunoritz.gradle.singularnode

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class ProjectFactory
{
	private ProjectFactory()
	{
		throw new UnsupportedOperationException()
	}

	static Project multiModuleProject()
	{
		def rootProject = rootProject()

		return subProject(rootProject)
	}

	static Project rootProject()
	{
		def project = ProjectBuilder.builder().build()

		project.plugins.apply('com.brunoritz.gradle.singular-node')

		return project
	}

	static Project subProject(Project rootProject)
	{
		def project = ProjectBuilder.builder()
			.withParent(rootProject)
			.build()

		project.plugins.apply('com.brunoritz.gradle.singular-node')

		return project
	}
}
