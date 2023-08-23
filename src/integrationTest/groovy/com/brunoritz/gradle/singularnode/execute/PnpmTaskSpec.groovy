package com.brunoritz.gradle.singularnode.execute

import com.brunoritz.gradle.singularnode.NodeJsExtension
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.brunoritz.gradle.singularnode.MockNodeInstallation.simulateNodeInstallationInProject
import static com.brunoritz.gradle.singularnode.ProjectFactory.multiModuleProject
import static com.brunoritz.gradle.singularnode.platform.InstallationLayoutFactory.platformDependentLayout

class PnpmTaskSpec
	extends Specification
{
	def 'It shell ensure packages are installed prior to executing'()
	{
		given:
			def subproject = multiModuleProject()
			def installTask = subproject.tasks.getByName('installPnpmPackages')

		when:
			def task = subproject.tasks.create('pnpmTask', PnpmTask)

		then:
			task.taskDependencies.getDependencies(task).contains(installTask)
	}

	@IgnoreIf({ System.getProperty('os.name').containsIgnoreCase('windows') })
	def 'It shall use the downloaded (global) version of PNPM to run commands'()
	{
		given:
			def subproject = multiModuleProject()
			def configuration = subproject.rootProject.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = subproject.tasks.create('pnpmTask', PnpmTask)

			subproject.projectDir.mkdirs()
			simulateNodeInstallationInProject(subproject.rootProject)

		when:
			task.execute()

		then:
			def pnpmCommand = subproject.file('cli.txt')
			def pnpmScript = layout.pathOfManagedPnpmScript()

			pnpmCommand.text.trim() == "${pnpmScript}"
	}

	@IgnoreIf({ System.getProperty('os.name').containsIgnoreCase('windows') })
	def 'It shall be possible to pass arguments'()
	{
		given:
			def subProject = multiModuleProject()
			def configuration = subProject.rootProject.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = subProject.tasks.create('pnpmTask', PnpmTask)

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
			def pnpmCommand = subProject.file('cli.txt')
			def pnpmScript = layout.pathOfManagedPnpmScript()

			pnpmCommand.text.trim() == "${pnpmScript} run foo --foo=bar"
	}
}
