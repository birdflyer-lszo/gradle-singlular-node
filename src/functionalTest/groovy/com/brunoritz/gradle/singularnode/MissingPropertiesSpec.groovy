package com.brunoritz.gradle.singularnode

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class MissingPropertiesSpec
	extends Specification
{
	def 'The NdeJS installation shall fail, if no version has been specified'()
	{
		given:
			def rootProjectDir = projectWithNodeVersion('')

		when:
			def result = GradleRunner.create()
				.withProjectDir(rootProjectDir)
				.withArguments(':installNodeJs')
				.withPluginClasspath()
				.buildAndFail()

		then:
			result.output.contains('Cannot query the value of this property')

		cleanup:
			rootProjectDir.deleteOnExit()
	}

	def 'The NPM installation shall fail, if no version has been specified'()
	{
		given:
			def rootProjectDir = projectWithNodeVersion('14.17.6')

		when:
			def result = GradleRunner.create()
				.withProjectDir(rootProjectDir)
				.withArguments(':installNpm')
				.withPluginClasspath()
				.buildAndFail()

		then:
			result.task(':installNodeJs').outcome == SUCCESS
			result.task(':installNpm').outcome == FAILED
			result.output.contains('property \'npmVersion\' doesn\'t have a configured value')

		cleanup:
			rootProjectDir.deleteOnExit()
	}

	def 'The PNPM installation shall fail, if no version has been specified'()
	{
		given:
			def rootProjectDir = projectWithNodeVersion('14.17.6')

		when:
			def result = GradleRunner.create()
				.withProjectDir(rootProjectDir)
				.withArguments(':installPnpm')
				.withPluginClasspath()
				.buildAndFail()

		then:
			result.task(':installNodeJs').outcome == SUCCESS
			result.task(':installPnpm').outcome == FAILED
			result.output.contains('property \'pnpmVersion\' doesn\'t have a configured value')

		cleanup:
			rootProjectDir.deleteOnExit()
	}

	def 'The Yarn installation shall fail, if no version has been specified'()
	{
		given:
			def rootProjectDir = projectWithNodeVersion('14.17.6')

		when:
			def result = GradleRunner.create()
				.withProjectDir(rootProjectDir)
				.withArguments(':installYarn')
				.withPluginClasspath()
				.buildAndFail()

		then:
			result.task(':installNodeJs').outcome == SUCCESS
			result.task(':installYarn').outcome == FAILED
			result.output.contains('property \'yarnVersion\' doesn\'t have a configured value')

		cleanup:
			rootProjectDir.deleteOnExit()
	}

	private File projectWithNodeVersion(String nodeVersion)
	{
		def rootProjectDir = File.createTempDir()
		def subProjectDir = new File(rootProjectDir, 'subproject')
		def nodeVersionQuoted = nodeVersion.length() > 0
			? "'${nodeVersion}'"
			: 'null'

		subProjectDir.mkdirs()

		new File(rootProjectDir, 'build.gradle') << """
				plugins {
				    id 'com.brunoritz.gradle.singular-node'
				}

				nodeJs {
					nodeVersion.set(${nodeVersionQuoted})
				}
			"""

		new File(subProjectDir, 'build.gradle') << '''
				plugins {
				    id 'com.brunoritz.gradle.singular-node'
				}
			'''

		new File(rootProjectDir, 'settings.gradle') << '''
				include ':subproject'
			'''

		return rootProjectDir
	}
}
