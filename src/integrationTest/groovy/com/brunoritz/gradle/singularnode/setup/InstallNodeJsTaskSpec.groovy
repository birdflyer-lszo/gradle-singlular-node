package com.brunoritz.gradle.singularnode.setup

import com.brunoritz.gradle.singularnode.NodeJsExtension
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.nio.file.Files

import static com.brunoritz.gradle.singularnode.ProjectFactory.rootProject
import static com.brunoritz.gradle.singularnode.platform.InstallationLayoutFactory.platformDependentLayout

class InstallNodeJsTaskSpec
	extends Specification
{
	def 'It shall remove an existing installation prior to installing'()
	{
		given:
			def project = rootProject()
			def configuration = project.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def simulatedLeftover = new File(layout.nodeJsInstallDir(), 'should-not-exist')
			def task = project.tasks.getByPath('installNodeJs')

			task.nodeArchive.set(archriveResourceAsFile('nodejs-windows.zip'))
			layout.nodeJsInstallDir().mkdirs()
			simulatedLeftover.createNewFile()

		when:
			task.installNode()

		then:
			!simulatedLeftover.exists()
	}

	@IgnoreIf({ System.getProperty('os.name').containsIgnoreCase('windows') })
	def 'It shall fix the NPM and NPX shortcuts on Unix operating systems'()
	{
		given:
			def project = rootProject()
			def configuration = project.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = project.tasks.getByPath('installNodeJs')

			task.nodeArchive.set(archriveResourceAsFile('nodejs-unix.tar.gz'))
			layout.nodeJsInstallDir().mkdirs()

		when:
			task.installNode()

		then:
			def sanitzedInstallPath = configuration.installBaseDir.dir('node/bin').get().asFile
			def npmLink = layout.pathOfBundledNpmScript()
			def npxLink = layout.pathOfBundnledNpxScript()

			sanitzedInstallPath.exists() && sanitzedInstallPath.isDirectory()
			Files.isSymbolicLink(npmLink.toPath())
			Files.isSymbolicLink(npxLink.toPath())
	}

	def 'It shall just extract the archive on Window systems'()
	{
		given:
			def project = rootProject()
			def configuration = project.extensions.getByType(NodeJsExtension)
			def layout = platformDependentLayout(configuration.installBaseDir).get()
			def task = project.tasks.getByPath('installNodeJs')

			task.nodeArchive.set(archriveResourceAsFile('nodejs-windows.zip'))
			layout.nodeJsInstallDir().mkdirs()

		when:
			task.installNode()

		then:
			def sanitizedNpmCmd = configuration.installBaseDir.dir('node/npm.cmd').get().asFile

			sanitizedNpmCmd.exists() && sanitizedNpmCmd.isFile()
	}

	private File archriveResourceAsFile(String identifier)
	{
		String path = getClass().getResource(identifier).getFile()

		return new File(path)
	}
}
