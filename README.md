# Singular Node Installation Plugin

[![Build](https://github.com/birdflyer-lszo/gradle-singlular-node/actions/workflows/build.yaml/badge.svg?branch=master)](https://github.com/birdflyer-lszo/gradle-singlular-node/actions/workflows/build.yaml)
[![License](https://img.shields.io/github/license/node-gradle/gradle-node-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
![Version](https://img.shields.io/badge/Version-1.0.0-orange.svg)

## About

The _Singular Node Installation_ plugin provides a single installation of NodeJS, Yarn, PNPM and NPM throughout the
entire project. Its intention is to reduce build time and complexity by only installing the tooling once and then allow
any subproject to consume it. It further reduces complexity by forcing the project to use a single version of the
tooling.

### Function

The root project has to apply the plugin and provide details on the version of the tooling to install. Any subproject
that applies the plugin can just consume the tooling installed by the plugin.

## Installation

The plugin can be applied to any project using the `plugins` closure. In order for the plugin to work, it has to be
applied on the root project as well.

```groovy
plugins {
	id 'com.brunoritz.gradle.singular-node' version '<version>'
}
```

Note that since the plugin is contained in a private Maven repository, that repository will need to be configured,
preferably in `settings.gradle`.

```groovy
pluginManagement {
	repositories {
		maven {
			url = '<private-repo-url>'
		}
	}
}
```

## Configuration

The plugin can be configured using the `nodeJs` extension.

```groovy
nodeJs {
	nodeVersion.set('14.17.6')
	pnpmVersion.set('6.23.2')
	yarnVersion.set('berry')
}
```

The following properties are available. Those marked with an asterisk are mandatory. Details on the default values and
further behavior can be found in the Javadoc documentation.

| Name              | Description                                                       |
|-------------------|-------------------------------------------------------------------|
| `nodeVersion`*    | The version of NodeJS to install                                  |
| `pnpmVersion`*    | The version of PNPM to install                                    |
| `yarnVersion`*    | The version of Yarn to install                                    |
| `installBaseDir`  | The base directory where NodeJS and Yarn are to be installed into |
| `pnpmInstallArgs` | Additional arguments to pass to PNPM for installing packages      |

To pass further arguments to the installation command, the `installYarnPackages` and `installPnpm` tasks support an
`args` property.

## Usage

Any non-root project applying the plugin gets `installYarnPackages` and `installPnpmPackages` tasks configured.
Any `YarnTask` or `PnpmTask` they define will automatically depend on the package installation tasks to ensure the
package installation.

Any node task will use the runtime environment declared in the root project.

In its simplest form, the root project needs

```groovy
plugins {
	id 'com.brunoritz.gradle.singular-node' version '<version>'
}

nodeJs {
	nodeVersion.set('<version>')
	pnpmVersion.set('<version>')
	yarnVersion.set('<version>')
}
```

A subproject can then configure tasks. The `nodeJs` extension will not be avaiable in the subproject itself. It is only
available on the root project.

```groovy
plugins {
	id 'com.brunoritz.gradle.singular-node' version '<version>'
}

task buildAngular(type: YarnTask) {
	args.set([
		'run', 'build', '--',
		'--aot',
		'--no-progress'
	])
}

task buildAngular(type: PnpmTask) {
	args.set([
		'run', 'build'
	])
}
```

If needed, `YarnTask` and `PnpmTask` allow passing environment variables via the `environment` property.

```groovy
task someYarnTask(tpe: YarnTask) {
	args.set(['run', 'my-script'])
	environment.set([
		SOME_VAR: 'some-value'
	])
}

task someNpmTask(tpe: PnpmTask) {
	args.set(['run', 'my-script'])
	environment.set([
		SOME_VAR: 'some-value'
	])
}
```

Should a project need to call Node directly without any of the task wrappers, it can obtain the location of all relevant
scripts via the `managedNodeJs` extenion.

The extension provides the following properties.

| Name               | Description                                         |
|--------------------|-----------------------------------------------------|
| `nodeJsExecutable` | The path to the NodeJS binary                       |
| `pnpmScript`       | The path to the managed PNPM script                 |
| `yarnScript`       | The path to the managed Yarn script                 |
| `nodeJsBinDir`     | The directory in which the NodeJS binary is located |

## Development Documentation

* [Code Style](doc/code-style.md)
* [Change Log](doc/changelog.md)
