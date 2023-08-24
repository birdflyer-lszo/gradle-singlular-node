package com.brunoritz.gradle.singularnode.platform

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class LookupSpec
	extends Specification
{
	def 'It shall return the plugin extension, if the plugin has been applied on the root project'()
	{
		given:
			def rootProject = ProjectBuilder.builder().build()
			def subproject = ProjectBuilder.builder()
				.withParent(rootProject)
				.build()

			rootProject.plugins.apply('com.brunoritz.gradle.singular-node')

		when:
			def result = Lookup.pluginConfiguration(subproject)

		then:
			result.isDefined()
	}

	def 'It shall return none, if the plugin is mising on the root project'()
	{
		given:
			def rootProject = ProjectBuilder.builder().build()
			def subproject = ProjectBuilder.builder()
				.withParent(rootProject)
				.build()

		when:
			def result = Lookup.pluginConfiguration(subproject)

		then:
			!result.isDefined()
	}

	def 'It shall return the given task from the root project, if it exists'()
	{
		given:
			def rootProject = ProjectBuilder.builder().build()
			def subproject = ProjectBuilder.builder()
				.withParent(rootProject)
				.build()

			rootProject.plugins.apply('com.brunoritz.gradle.singular-node')

		when:
			def result = Lookup.rootProjectTask(subproject, 'installNodeJs')

		then:
			result.isDefined()
	}

	def 'It shall return none, if the given task does not exist on the root project'()
	{
		given:
			def rootProject = ProjectBuilder.builder().build()
			def subproject = ProjectBuilder.builder()
				.withParent(rootProject)
				.build()

		when:
			def result = Lookup.rootProjectTask(subproject, 'installNodeJs')

		then:
			!result.isDefined()
	}
}
