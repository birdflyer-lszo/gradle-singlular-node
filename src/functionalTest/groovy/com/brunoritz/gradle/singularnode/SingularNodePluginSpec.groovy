package com.brunoritz.gradle.singularnode

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SingularNodePluginSpec
	extends Specification
{
	def rootProjectDir
	def subProjectDir
	def rootBuildFile
	def subProjectBuildFile
	def settingsFile

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
					pnpmVersion.set('6.23.2')
				}
			'''

		settingsFile << '''
				include ':subproject'
			'''
	}

	def 'It shall be possible to run Yarn tasks'()
	{
		given:
			def packageFile = new File(subProjectDir, 'package.json')

			subProjectBuildFile << '''
				plugins {
				    id 'com.brunoritz.gradle.singular-node'
				}

				task runNodeScript(type: YarnTask) {
					args.set(['--help'])
				}
			'''

			packageFile << '{}'

		when:
			def result = GradleRunner.create()
				.withProjectDir(rootProjectDir)
				.withArguments(':subproject:runNodeScript')
				.withPluginClasspath()
				.build()

		then:
			result.output.contains('Yarn Package Manager - 2')
			result.task(':subproject:runNodeScript').outcome == SUCCESS
	}

	def 'It shall be possible to run PNPM tasks'()
	{
		given:
			def packageFile = new File(subProjectDir, 'package.json')

			subProjectBuildFile << '''
				plugins {
				    id 'com.brunoritz.gradle.singular-node'
				}

				task runNodeScript(type: PnpmTask) {
					args.set(['--help'])
				}
			'''

			packageFile << '{}'

		when:
			def result = GradleRunner.create()
				.withProjectDir(rootProjectDir)
				.withArguments('--configuration-cache', ':subproject:runNodeScript')
				.withPluginClasspath()
				.build()

		then:
			result.output.contains('Usage: pnpm [command] [flags]')
			result.task(':subproject:runNodeScript').outcome == SUCCESS
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

				task runNodeScript(type: YarnTask) {
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
				.withArguments('--configuration-cache', ':subproject:runNodeScript')
				.withPluginClasspath()
				.build()

		then:
			result.output.contains('bar-environment')
			result.output.contains('baz-environment')
			result.task(':subproject:runNodeScript').outcome == SUCCESS
	}

	def 'It shall be possible to pass environment variables to scripts via PNPM'()
	{
		given:
			def packageFile = new File(subProjectDir, 'package.json')
			def scriptFile = new File(subProjectDir, 'test.js')

			subProjectBuildFile << '''
				plugins {
				    id 'com.brunoritz.gradle.singular-node'
				}

				task runNodeScript(type: PnpmTask) {
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
				.withArguments('--configuration-cache', ':subproject:runNodeScript')
				.withPluginClasspath()
				.build()

		then:
			result.output.contains('bar-environment')
			result.output.contains('baz-environment')
			result.task(':subproject:runNodeScript').outcome == SUCCESS
	}
}