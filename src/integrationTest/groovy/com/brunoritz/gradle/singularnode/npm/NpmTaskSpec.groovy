package com.brunoritz.gradle.singularnode.npm

import com.brunoritz.gradle.singularnode.NodeJsExtension
import com.brunoritz.gradle.singularnode.yarn.YarnTask
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.brunoritz.gradle.singularnode.Configuration.configureNodeJs
import static com.brunoritz.gradle.singularnode.MockNodeInstallation.simulateNodeInstallationInProject
import static com.brunoritz.gradle.singularnode.ProjectFactory.multiModuleProject
import static com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory.platformDependentLayout

class NpmTaskSpec
	extends Specification
{
	def 'It shell ensure packages are installed prior to executing'()
	{
		given:
			def subproject = multiModuleProject()
			def installTask = subproject.tasks.getByName('installNpmPackages')

		when:
			def task = subproject.tasks.create('npmTask', NpmTask)

		then:
			task.taskDependencies.getDependencies(task).contains(installTask)
	}

	def 'It shall make the NodeJS and NPM versions property inputs for reliable caching and to-to-date checks'()
	{
		given:
			def subproject = multiModuleProject()

			configureNodeJs(subproject.rootProject) {
				nodeVersion.set('1.2.3')
				npmVersion.set('5.6.7')
			}

		when:
			def task = subproject.tasks.create('npmTask', NpmTask)

		then:
			task.inputs.properties['nodeJsVersion'] == '1.2.3'
			task.inputs.properties['npmVersion'] == '5.6.7'
	}

	@IgnoreIf({ System.getProperty('os.name').containsIgnoreCase('windows') })
	def 'It shall use the downloaded (global) version of NPM to run commands'()
	{
		given:
			def subproject = multiModuleProject()
			def configuration = subproject.rootProject.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = subproject.tasks.create('npmTask', NpmTask)

			subproject.projectDir.mkdirs()
			simulateNodeInstallationInProject(subproject.rootProject)

		when:
			task.execute()

		then:
			def npmCommand = subproject.file('cli.txt')
			def npmScript = layout.pathOfManagedNpmScript()

			npmCommand.text.trim() == "${npmScript}"
	}

	@IgnoreIf({ System.getProperty('os.name').containsIgnoreCase('windows') })
	def 'It shall be possible to pass arguments'()
	{
		given:
			def subProject = multiModuleProject()
			def configuration = subProject.rootProject.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = subProject.tasks.create('npmTask', NpmTask)

			subProject.projectDir.mkdirs()
			simulateNodeInstallationInProject(subProject.rootProject)

			task.args.set([
				'run',
				'foo',
				'--foo=bar'
			])

		when:
			task.execute()

		then:
			def npmCommand = subProject.file('cli.txt')
			def npmScript = layout.pathOfManagedNpmScript()

			npmCommand.text.trim() == "${npmScript} run foo --foo=bar"
	}
}
