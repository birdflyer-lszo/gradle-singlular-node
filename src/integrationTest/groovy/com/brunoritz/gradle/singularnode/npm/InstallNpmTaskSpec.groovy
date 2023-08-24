package com.brunoritz.gradle.singularnode.npm

import com.brunoritz.gradle.singularnode.NodeJsExtension
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.brunoritz.gradle.singularnode.MockNodeInstallation.simulateNodeInstallationInProject
import static com.brunoritz.gradle.singularnode.ProjectFactory.rootProject
import static com.brunoritz.gradle.singularnode.platform.layout.InstallationLayoutFactory.platformDependentLayout

@IgnoreIf({ System.getProperty('os.name').containsIgnoreCase('windows') })
class InstallNpmTaskSpec
	extends Specification
{
	def 'It shall remove an existing installation prior to installing'()
	{
		given:
			def project = rootProject()
			def configuration = project.extensions.getByType(NodeJsExtension)
			def installBase = simulateNodeInstallationInProject(project)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def simulatedLeftover = new File(layout.npmInstallDirectory(), 'should-not-exist')
			def task = project.tasks.getByPath('installNpm')

			configuration.npmVersion.set('1.2.3')
			layout.npmInstallDirectory().mkdirs()
			simulatedLeftover.createNewFile()

			configuration.installBaseDir.set(installBase)

		when:
			task.installNpm()

		then:
			!simulatedLeftover.exists()
	}

	def 'It shall use the bundled NPM to install the specified version of NPM'()
	{
		given:
			def project = rootProject()
			def configuration = project.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = project.tasks.getByPath('installNpm')

			simulateNodeInstallationInProject(project)
			configuration.npmVersion.set('1.2.3')

		when:
			task.installNpm()

		then:
			def npmCommand = project.file('cli.txt')
			def npmScript = layout.pathOfBundledNpmScript()
			def npmDir = layout.npmInstallDirectory()

			/*
			 * In order to save execution time, we do not actually install NPM. Otherwise this test would also require
			 * a full NodeJS installation. This might simply take too long to achieve. So, we only assert that the task
			 * would call NPM with the correct command line needed to install the specified version of NPM.
			 */
			npmCommand.text.trim() == "${npmScript} install " +
				'--global ' +
				'--no-save ' +
				"--prefix ${npmDir} " +
				'npm@1.2.3'
	}
}
