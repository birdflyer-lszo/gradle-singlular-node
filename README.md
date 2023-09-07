# Singular Node Installation Plugin

[![build](https://github.com/birdflyer-lszo/gradle-singlular-node/actions/workflows/build.yaml/badge.svg?branch=master&event=push)](https://github.com/birdflyer-lszo/gradle-singlular-node/actions/workflows/build.yaml)
[![License](https://img.shields.io/github/license/node-gradle/gradle-node-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
![Version](https://img.shields.io/badge/Version-1.2.0-orange.svg)

## About

The _Singular Node Installation_ plugin provides a single installation of NodeJS, Yarn, PNPM and NPM throughout the
entire project. Its intention is to reduce build time and complexity by only installing the tooling once and then allow
any subproject to consume it. It further reduces complexity by forcing the project to use a single version of the
tooling.

### Function

The root project has to apply the plugin and provide details on the version of the tooling to install. Any subproject
that applies the plugin can just consume the tooling installed by the plugin.

## Installation

The plugin can be applied to any project. In order for the plugin to work, it has to be applied on the root project as
well.

```groovy
plugins {
	id 'com.brunoritz.gradle.singular-node' version '<version>'
}
```

## Configuration

The plugin can be configured using the `nodeJs` extension.

```groovy
nodeJs {
	nodeVersion.set('14.17.6')
	npmVersion.set('6.14.18')
	pnpmVersion.set('6.23.2')
	yarnVersion.set('berry')
}
```

The following properties are available. Details on the default values and further behavior can be found in the Javadoc
documentation.

| Name              | Description                                                                        |
|-------------------|------------------------------------------------------------------------------------|
| `downloadBase`    | The base URL from which to download NodeJS (defaults to `https://nodejs.org/dist`) |
| `nodeVersion`     | The version of NodeJS to install                                                   |
| `npmVersion`      | The version of NPM to install                                                      |
| `pnpmVersion`     | The version of PNPM to install                                                     |
| `yarnVersion`     | The version of Yarn to install                                                     |
| `installBaseDir`  | The base directory where NodeJS and Yarn are to be installed into                  |
| `npmInstallArgs`  | Additional arguments to pass to NPM for installing packages                        |
| `pnpmInstallArgs` | Additional arguments to pass to PNPM for installing packages                       |
| `yarnInstallArgs` | Additional arguments to pass to Yarn for installing packages                       |

## Usage

Any non-root project applying the plugin gets `installNpmPackages`, `installPnpmPackages` and `installYarnPackages`
tasks configured. Note that the package installation tasks do not declare `node_modules` as an output directory in order
to avoid lengthy content scanning being performed by Gradle.

Any `NpmTask`, `PnpmTask` or `YarnTask` they define will automatically depend on the package installation tasks to
ensure the installation of the dependencies defined in `package.json`.

Any node task will use the runtime environment declared in the root project.

In its simplest form, the root project needs

```groovy
plugins {
	id 'com.brunoritz.gradle.singular-node' version '<version>'
}

nodeJs {
	nodeVersion.set('<version>')
	npmVersion.set('<version>')
	pnpmVersion.set('<version>')
	yarnVersion.set('<version>')
}
```

A subproject can then configure tasks. The `nodeJs` extension will not be available in the subproject itself. It is only
available on the root project.

```groovy
plugins {
	id 'com.brunoritz.gradle.singular-node' version '<version>'
}

task buildWithNpm(type: NpmTask) {
	args.set([
		'run', 'build'
	])
}

task buildWithPnpm(type: PnpmTask) {
	args.set([
		'run', 'build'
	])
}

task buildWithYarn(type: YarnTask) {
	args.set([
		'run', 'build'
	])
}
```

If needed, `NpmTask`, `PnpmTask` and `YarnTask` allow passing environment variables via the `environment` property.

```groovy
task someNpmTask(tpe: NpmTask) {
	args.set(['run', 'my-script'])
	environment.set([
		SOME_VAR: 'some-value'
	])
}

task somePnpmTask(tpe: PnpmTask) {
	args.set(['run', 'my-script'])
	environment.set([
		SOME_VAR: 'some-value'
	])
}

task someYarnTask(tpe: YarnTask) {
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
| `npmScript`        | The path to the managed NPM script                  |
| `pnpmScript`       | The path to the managed PNPM script                 |
| `yarnScript`       | The path to the managed Yarn script                 |
| `nodeJsBinDir`     | The directory in which the NodeJS binary is located |

## Development Documentation

* [Code Style](doc/code-style.md)
* [Change Log](doc/changelog.md)
