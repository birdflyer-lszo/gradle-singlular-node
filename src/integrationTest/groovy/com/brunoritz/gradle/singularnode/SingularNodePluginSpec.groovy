package com.brunoritz.gradle.singularnode

import com.brunoritz.gradle.singularnode.execute.PnpmTask
import com.brunoritz.gradle.singularnode.execute.YarnTask
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static ProjectFactory.rootProject
import static ProjectFactory.subProject

class SingularNodePluginSpec
	extends Specification
{
	def 'It shall create the NodeJS setup tasks on the root project'()
	{
		given:
			def rootProject = rootProject()

		expect:
			def nodeInstallTask = rootProject.tasks.findByPath('installNodeJs')
			def yarnInstallTask = rootProject.tasks.findByPath('installYarn')

			nodeInstallTask != null
			yarnInstallTask != null
			yarnInstallTask.taskDependencies.getDependencies(yarnInstallTask).contains(nodeInstallTask)
	}

	def 'It shall create no NodeJS setup task on a subproject'()
	{
		given:
			def rootProject = rootProject()
			def subProject = subProject(rootProject)

		expect:
			subProject.tasks.findByPath('installNodeJs') == null
			subProject.tasks.findByPath('installYarn') == null
	}

	def 'It shall create a task to install Node modules in subprojects'()
	{
		given:
			def rootProject = rootProject()
			def subProject = subProject(rootProject)

		expect:
			subProject.tasks.findByPath('installYarnPackages') != null
			subProject.tasks.findByPath('installPnpmPackages') != null
	}

	def 'It shall publish a global task type for simplified Yarn task authoring'()
	{
		given:
			def rootProject = rootProject()
			def subProject = subProject(rootProject)

		expect:
			subProject.extensions.extraProperties.get('YarnTask') == YarnTask
	}

	def 'It shall publish a global task type for simplified PNPM task authoring'()
	{
		given:
			def rootProject = rootProject()
			def subProject = subProject(rootProject)

		expect:
			subProject.extensions.extraProperties.get('PnpmTask') == PnpmTask
	}

	def 'It shall publish the paths to the managed NodeJS, Yarn and PNPM scripts'()
	{
		given:
			def rootProject = rootProject()
			def subProject = subProject(rootProject)

		when:
			def nodeBinDir = subProject.extensions.getByType(ManagedNodeJs).nodeJsBinDir
			def nodeExecutable = subProject.extensions.getByType(ManagedNodeJs).nodeJsExecutable
			def yarnScript = subProject.extensions.getByType(ManagedNodeJs).yarnScript
			def pnpmScript = subProject.extensions.getByType(ManagedNodeJs).pnpmScript

		then:
			nodeBinDir.name.endsWith('bin')      || nodeBinDir.name.endsWith('node')
			nodeExecutable.name.endsWith('node') || nodeExecutable.name.endsWith('node.exe')
			yarnScript.name.endsWith('yarn')     || yarnScript.name.endsWith('yarn.js')
			pnpmScript.name.endsWith('pnpm')     || pnpmScript.name.endsWith('pnpm.cjs')
	}

	def 'It shall add an Ivy repository to download NodeJS distributions'()
	{
		given:
			def rootProject = rootProject()

		when:
			def repository = rootProject.repositories.findByName('nodejs')

		then:
			/*
             * The repository interface does not permit much more validation than what is below. In order to finally
             * validate the proper working of the repository a functional test of the plugin is needed.
             */
			repository != null
			IvyArtifactRepository.isInstance(repository)
			repository.url.toString() == 'https://nodejs.org/dist'
	}

	def 'It shall produce an error, if the plugin is applied to a subproject, but missing on the root project'()
	{
		given:
			def rootProject = ProjectBuilder.builder().build()
			def subProject = ProjectBuilder.builder()
				.withParent(rootProject)
				.build()

		when:
			subProject.plugins.apply('com.brunoritz.gradle.singular-node')

		then:
			def error = thrown(PluginApplicationException)

			error.cause.class == IllegalStateException
	}
}