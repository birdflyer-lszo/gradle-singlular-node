package com.brunoritz.gradle.singularnode

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PluginSpecYarn
	extends Specification
{
	private File rootProjectDir
	private File subProjectDir
	private File rootBuildFile
	private File subProjectBuildFile
	private File settingsFile

	def setup()
	{
		rootProjectDir = File.createTempDir()
		subProjectDir = new File(rootProjectDir, 'subproject')
		rootBuildFile = new File(rootProjectDir, 'build.gradle')
		subProjectBuildFile = new File(subProjectDir, 'build.gradle')
		settingsFile = new File(rootProjectDir, 'settings.gradle')

		subProjectDir.mkdirs()

		rootBuildFile << '''
				plugins {
				    id 'com.brunoritz.gradle.singular-node'
				}

				nodeJs {
					nodeVersion.set('14.17.6')
					yarnVersion.set('berry')
				}
			'''

		settingsFile << '''
				include ':subproject'
			'''
	}

	def cleanup()
	{
		rootProjectDir.deleteOnExit()
	}

	def 'It shall be possible to run Yarn tasks'()
	{
		given:
			def packageFile = new File(subProjectDir, 'package.json')

			subProjectBuildFile << '''
				plugins {
				    id 'com.brunoritz.gradle.singular-node'
				}

				task runYarn(type: YarnTask) {
					args.set(['--help'])
				}
			'''

			packageFile << '{}'

		when:
			def result = GradleRunner.create()
				.withProjectDir(rootProjectDir)
				.withArguments(':subproject:runYarn')
				.withPluginClasspath()
				.build()

		then:
			result.output.contains('Yarn Package Manager - 2')
			result.task(':subproject:runYarn').outcome == SUCCESS
	}

	def 'It shall be possible to pass environment variables to scripts via Yarn'()
	{
		given:
			def packageFile = new File(subProjectDir, 'package.json')
			def scriptFile = new File(subProjectDir, 'test.js')

			subProjectBuildFile << '''
				plugins {
				    id 'com.brunoritz.gradle.singular-node'
				}

				task runYarn(type: YarnTask) {
					args.set([
						'run',
						'test'
					])

					environment.set([
						FOO: 'bar-environment',
						BAR: 'baz-environment'
					])
				}
			'''

			scriptFile << 'console.log(process.env.FOO, process.env.BAR)'

			packageFile << '''
				{
					"scripts": {
						"test": "node test.js"
					}
		   		}
			'''

		when:
			def result = GradleRunner.create()
				.withProjectDir(rootProjectDir)
				.withArguments('--configuration-cache', ':subproject:runYarn')
				.withPluginClasspath()
				.build()

		then:
			result.output.contains('bar-environment')
			result.output.contains('baz-environment')
			result.task(':subproject:runYarn').outcome == SUCCESS
	}
}
