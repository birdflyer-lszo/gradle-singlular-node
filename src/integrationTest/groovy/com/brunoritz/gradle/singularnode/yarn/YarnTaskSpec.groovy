package com.brunoritz.gradle.singularnode.yarn

import com.brunoritz.gradle.singularnode.NodeJsExtension
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.brunoritz.gradle.singularnode.Configuration.configureNodeJs
import static com.brunoritz.gradle.singularnode.MockNodeInstallation.simulateNodeInstallationInProject
import static com.brunoritz.gradle.singularnode.ProjectFactory.multiModuleProject
import static com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory.platformDependentLayout

class YarnTaskSpec
	extends Specification
{
	def 'It shell ensure packages are installed prior to executing'()
	{
		given:
			def subproject = multiModuleProject()
			def installTask = subproject.tasks.getByName('installYarnPackages')

		when:
			def task = subproject.tasks.create('yarnTask', YarnTask)

		then:
			task.taskDependencies.getDependencies(task).contains(installTask)
	}

	def 'It shall make the NodeJS and Yarn versions property inputs for reliable caching and to-to-date checks'()
	{
		given:
			def subproject = multiModuleProject()

			configureNodeJs(subproject.rootProject) {
				nodeVersion.set('1.2.3')
				yarnVersion.set('berry')
			}

		when:
			def task = subproject.tasks.create('yarnTask', YarnTask)

		then:
			task.inputs.properties['nodeJsVersion'] == '1.2.3'
			task.inputs.properties['yarnVersion'] == 'berry'
	}

	@IgnoreIf({ System.getProperty('os.name').containsIgnoreCase('windows') })
	def 'It shall use the downloaded (global) version of Yarn to run commands'()
	{
		given:
			def subproject = multiModuleProject()
			def configuration = subproject.rootProject.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = subproject.tasks.create('yarnTask', YarnTask)

			subproject.projectDir.mkdirs()
			simulateNodeInstallationInProject(subproject.rootProject)

		when:
			task.execute()

		then:
			def yarnCommand = subproject.file('cli.txt')
			def yarnScript = layout.pathOfManagedYarnScript()

			yarnCommand.text.trim() == "${yarnScript}"
	}

	@IgnoreIf({ System.getProperty('os.name').containsIgnoreCase('windows') })
	def 'It shall be possible to pass arguments'()
	{
		given:
			def subProject = multiModuleProject()
			def configuration = subProject.rootProject.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = subProject.tasks.create('yarnTask', YarnTask)

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
			def yarnCommand = subProject.file('cli.txt')
			def yarnScript = layout.pathOfManagedYarnScript()

			yarnCommand.text.trim() == "${yarnScript} run foo --foo=bar"
	}
}
