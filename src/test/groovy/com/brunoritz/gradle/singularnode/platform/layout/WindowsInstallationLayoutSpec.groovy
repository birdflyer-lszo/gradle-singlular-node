package com.brunoritz.gradle.singularnode.platform.layout

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class WindowsInstallationLayoutSpec
	extends Specification
{
	def 'It shall provide consumers with the installation directory of NodeJS'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new WindowsInstallationLayout(baseDirectory).nodeJsInstallDir()

		then:
			result == project.file('base-path/node')
	}

	def 'It shall provide consumers with the installation directory of NPM'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new WindowsInstallationLayout(baseDirectory).npmInstallDirectory()

		then:
			result == project.file('base-path/npm')
	}

	def 'It shall provide consumers with the installation directory of Yarn'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new WindowsInstallationLayout(baseDirectory).yarnInstallDirectory()

		then:
			result == project.file('base-path/yarn')
	}

	def 'It shall provide consumers with the installation directory of PNPM'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new WindowsInstallationLayout(baseDirectory).pnpmInstallDirectory()

		then:
			result == project.file('base-path/pnpm')
	}

	def 'It shall provide consumers with the binaries directory of NodeJS installation'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new WindowsInstallationLayout(baseDirectory).nodeJsBinDirectory()

		then:
			result == project.file('base-path/node')
	}

	def 'It shall provide consumers with the path to the NodeJS executable'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new WindowsInstallationLayout(baseDirectory).pathOfNodeExecutable()

		then:
			result == project.file('base-path/node/node.exe')
	}

	def 'It shall provide consumers with the path to the bundled NPM script'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new WindowsInstallationLayout(baseDirectory).pathOfBundledNpmScript()

		then:
			result == project.file('base-path/node/node_modules/npm/bin/npm-cli.js')
	}

	def 'It shall provide consumers with the path to the bundled NPX script'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new WindowsInstallationLayout(baseDirectory).pathOfBundnledNpxScript()

		then:
			result == project.file('base-path/node/node_modules/npm/bin/npx-cli.js')
	}

	def 'It shall provide consumers with the path to bundled NPM CLI scripts'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new WindowsInstallationLayout(baseDirectory).pathOfBundledCliScript('foo-bar')

		then:
			result == project.file('base-path/node/node_modules/npm/bin/foo-bar-cli.js')
	}

	def 'It shall providers consumers with the path to the installed Yarn script'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new WindowsInstallationLayout(baseDirectory).pathOfManagedYarnScript()

		then:
			result == project.file('base-path/yarn/node_modules/yarn/bin/yarn.js')
	}

	def 'It shall providers consumers with the path to the installed PNPM script'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new WindowsInstallationLayout(baseDirectory).pathOfManagedPnpmScript()

		then:
			result == project.file('base-path/pnpm/node_modules/pnpm/bin/pnpm.cjs')
	}

	private static Project newProject()
	{
		def project = ProjectBuilder.builder().build()

		project.plugins.apply('com.brunoritz.gradle.singular-node')

		return project
	}
}
