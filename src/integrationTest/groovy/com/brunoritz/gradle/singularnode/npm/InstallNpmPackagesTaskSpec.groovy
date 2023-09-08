package com.brunoritz.gradle.singularnode.npm

import com.brunoritz.gradle.singularnode.NodeJsExtension
import com.brunoritz.gradle.singularnode.nodejs.InstallNodeJsTask
import org.gradle.api.Project
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.brunoritz.gradle.singularnode.MockNodeInstallation.simulateNodeInstallationInProject
import static com.brunoritz.gradle.singularnode.ProjectFactory.multiModuleProject
import static com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory.platformDependentLayout

/*
 * In order to save execution time, we do not actually install packages. Otherwise this test would also require a
 * full NodeJS installation. This might simply take too long to achieve. So, we only assert that the task would call
 * NPM with the correct command line needed to install the packages.
 */

@IgnoreIf({ System.getProperty('os.name').containsIgnoreCase('windows') })
class InstallNpmPackagesTaskSpec
	extends Specification
{
	def 'It shall use the downloaded (global) version of NPM to install the project packages'()
	{
		given:
			def subProject = multiModuleProject()
			def configuration = subProject.rootProject.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = installTaskFromProject(subProject)

			subProject.projectDir.mkdirs()
			subProject.file('node_modules').mkdirs()
			simulateNodeInstallationInProject(subProject.rootProject)

		when:
			task.installPackages()

		then:
			def npmCommand = subProject.file('cli.txt')
			def npmScript = layout.pathOfManagedNpmScript()

			npmCommand.text.trim() == "${npmScript} install"
	}

	def 'It shall be possible to pass arguments to the install command'()
	{
		given:
			def subProject = multiModuleProject()
			def configuration = subProject.rootProject.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = installTaskFromProject(subProject)

			subProject.projectDir.mkdirs()
			subProject.file('node_modules').mkdirs()
			simulateNodeInstallationInProject(subProject.rootProject)

			configuration.npmInstallArgs.set([
				'--prefer-offline',
				'--no-save'
			])

		when:
			task.installPackages()

		then:
			def npmCommand = subProject.file('cli.txt')
			def npmScript = layout.pathOfManagedNpmScript()

			npmCommand.text.trim() == "${npmScript} install --prefer-offline --no-save"
	}

	def 'It shall create a marker file to indicate successful execution'()
	{
		given:
			def subProject = multiModuleProject()
			def task = installTaskFromProject(subProject)

			subProject.projectDir.mkdirs()
			subProject.file('node_modules').mkdirs()
			simulateNodeInstallationInProject(subProject.rootProject)

		when:
			task.installPackages()

		then:
			new File(subProject.file('node_modules'), '.install.executed').exists()
	}

	private static InstallNpmPackagesTask installTaskFromProject(Project project)
	{
		return project.tasks.getByPath('installNpmPackages') as InstallNpmPackagesTask
	}
}
