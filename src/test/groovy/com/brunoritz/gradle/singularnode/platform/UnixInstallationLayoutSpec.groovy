package com.brunoritz.gradle.singularnode.platform

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class UnixInstallationLayoutSpec
	extends Specification
{
	def 'It shall provide consumers with the installation directory of NodeJS'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new UnixInstallationLayout(baseDirectory).nodeJsInstallDir()

		then:
			result == project.file('base-path/node')
	}

	def 'It shall provide consumers with the installation directory of Yarn'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new UnixInstallationLayout(baseDirectory).yarnInstallDirectory()

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
			def result = new UnixInstallationLayout(baseDirectory).pnpmInstallDirectory()

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
			def result = new UnixInstallationLayout(baseDirectory).nodeJsBinDirectory()

		then:
			result == project.file('base-path/node/bin')
	}

	def 'It shall provide consumers with the path to the NodeJS executable'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new UnixInstallationLayout(baseDirectory).pathOfNodeExecutable()

		then:
			result == project.file('base-path/node/bin/node')
	}

	def 'It shall provide consumers with the path to the bundled NPM script'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new UnixInstallationLayout(baseDirectory).pathOfBundledNpmScript()

		then:
			result == project.file('base-path/node/bin/npm')
	}

	def 'It shall provide consumers with the path to the bundled NPX script'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new UnixInstallationLayout(baseDirectory).pathOfBundnledNpxScript()

		then:
			result == project.file('base-path/node/bin/npx')
	}

	def 'It shall provide consumers with the path to bundled NPM CLI scripts'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new UnixInstallationLayout(baseDirectory).pathOfBundledCliScript('foo-bar')

		then:
			result == project.file('base-path/node/lib/node_modules/npm/bin/foo-bar-cli.js')
	}

	def 'It shall providers consumers with the path to the installed Yarn script'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new UnixInstallationLayout(baseDirectory).pathOfManagedYarnScript()

		then:
			result == project.file('base-path/yarn/bin/yarn')
	}

	def 'It shall providers consumers with the path to the installed PNPM script'()
	{
		given:
			def project = newProject()
			def baseDirectory = project.objects.directoryProperty()

			baseDirectory.set(project.file('base-path'))

		when:
			def result = new UnixInstallationLayout(baseDirectory).pathOfManagedPnpmScript()

		then:
			result == project.file('base-path/pnpm/bin/pnpm')
	}

	private static Project newProject()
	{
		def project = ProjectBuilder.builder().build()

		project.plugins.apply('com.brunoritz.gradle.singular-node')

		return project
	}
}
