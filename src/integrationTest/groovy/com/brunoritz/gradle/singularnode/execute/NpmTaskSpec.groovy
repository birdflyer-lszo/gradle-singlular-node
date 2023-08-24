package com.brunoritz.gradle.singularnode.execute

import com.brunoritz.gradle.singularnode.NodeJsExtension
import spock.lang.IgnoreIf
import spock.lang.Specification

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
