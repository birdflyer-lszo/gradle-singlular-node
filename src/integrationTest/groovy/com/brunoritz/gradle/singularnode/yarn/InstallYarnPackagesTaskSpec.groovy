package com.brunoritz.gradle.singularnode.yarn

import com.brunoritz.gradle.singularnode.NodeJsExtension
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.brunoritz.gradle.singularnode.MockNodeInstallation.simulateNodeInstallationInProject
import static com.brunoritz.gradle.singularnode.ProjectFactory.multiModuleProject
import static com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory.platformDependentLayout

/*
 * In order to save execution time, we do not actually install packages. Otherwise this test would also require a
 * full NodeJS installation. This might simply take too long to achieve. So, we only assert that the task would call
 * Yarn with the correct command line needed to install the packages.
 */

@IgnoreIf({ System.getProperty('os.name').containsIgnoreCase('windows') })
class InstallYarnPackagesTaskSpec
	extends Specification
{
	def 'It shall use the downloaded (global) version of Yarn to install the project packages'()
	{
		given:
			def subProject = multiModuleProject()
			def configuration = subProject.rootProject.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = subProject.tasks.getByPath('installYarnPackages')

			subProject.projectDir.mkdirs()
			subProject.file('node_modules').mkdirs()
			simulateNodeInstallationInProject(subProject.rootProject)

		when:
			task.installPackages()

		then:
			def yarnCommand = subProject.file('cli.txt')
			def yarnScript = layout.pathOfManagedYarnScript()

			yarnCommand.text.trim() == "${yarnScript} install"
	}

	def 'It shall be possible to pass arguments to the install command'()
	{
		given:
			def subProject = multiModuleProject()
			def configuration = subProject.rootProject.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = subProject.tasks.getByPath('installYarnPackages')

			subProject.projectDir.mkdirs()
			subProject.file('node_modules').mkdirs()
			simulateNodeInstallationInProject(subProject.rootProject)

			configuration.yarnInstallArgs.set([
				'--prefer-offline',
				'--no-save'
			])

		when:
			task.installPackages()

		then:
			def yarnCommand = subProject.file('cli.txt')
			def yarnScript = layout.pathOfManagedYarnScript()

			yarnCommand.text.trim() == "${yarnScript} install --prefer-offline --no-save"
	}

	def 'It shall create a marker file to indicate successful execution'()
	{
		given:
			def subProject = multiModuleProject()
			def task = subProject.tasks.getByPath('installYarnPackages')

			subProject.projectDir.mkdirs()
			subProject.file('node_modules').mkdirs()
			simulateNodeInstallationInProject(subProject.rootProject)

		when:
			task.installPackages()

		then:
			new File(subProject.file('node_modules'), '.install.executed').exists()
	}
}
