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
					npmVersion.set('6.14.18')
					yarnVersion.set('berry')
					pnpmVersion.set('6.23.2')
				}
			'''

		settingsFile << '''
				include ':subproject'
			'''
	}

	def 'It shall be possible to run NPM tasks'()
	{
		given:
			def packageFile = new File(subProjectDir, 'package.json')

			subProjectBuildFile << '''
				plugins {
				    id 'com.brunoritz.gradle.singular-node'
				}

				task runNpm(type: NpmTask) {
					args.set(['version'])
				}
			'''

			packageFile << '{}'

		when:
			def result = GradleRunner.create()
				.withProjectDir(rootProjectDir)
				.withArguments(':subproject:runNpm')
				.withPluginClasspath()
				.build()

		then:
			result.output.contains('npm: \'6.14.18\'')
			result.task(':subproject:runNpm').outcome == SUCCESS
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
				.forwardOutput()
				.build()

		then:
			result.output.contains('Yarn Package Manager - 2')
			result.task(':subproject:runYarn').outcome == SUCCESS
	}

	def 'It shall be possible to run PNPM tasks'()
	{
		given:
			def packageFile = new File(subProjectDir, 'package.json')

			subProjectBuildFile << '''
				plugins {
				    id 'com.brunoritz.gradle.singular-node'
				}

				task runPnpm(type: PnpmTask) {
					args.set(['--help'])
				}
			'''

			packageFile << '{}'

		when:
			def result = GradleRunner.create()
				.withProjectDir(rootProjectDir)
				.withArguments('--configuration-cache', ':subproject:runPnpm')
				.withPluginClasspath()
				.forwardOutput()
				.build()

		then:
			result.output.contains('Usage: pnpm [command] [flags]')
			result.task(':subproject:runPnpm').outcome == SUCCESS
	}

	def 'It shall be possible to pass environment variables to scripts via NPM'()
	{
		given:
			def packageFile = new File(subProjectDir, 'package.json')
			def scriptFile = new File(subProjectDir, 'test.js')

			subProjectBuildFile << '''
				plugins {
				    id 'com.brunoritz.gradle.singular-node'
				}

				task runNpm(type: NpmTask) {
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
				.withArguments('--configuration-cache', ':subproject:runNpm')
				.withPluginClasspath()
				.forwardOutput()
				.build()

		then:
			result.output.contains('bar-environment')
			result.output.contains('baz-environment')
			result.task(':subproject:runNpm').outcome == SUCCESS
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
				.forwardOutput()
				.build()

		then:
			result.output.contains('bar-environment')
			result.output.contains('baz-environment')
			result.task(':subproject:runYarn').outcome == SUCCESS
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

				task runPnpm(type: PnpmTask) {
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
				.withArguments('--configuration-cache', ':subproject:runPnpm')
				.withPluginClasspath()
				.forwardOutput()
				.build()

		then:
			result.output.contains('bar-environment')
			result.output.contains('baz-environment')
			result.task(':subproject:runPnpm').outcome == SUCCESS
	}
}
