# Change Log

## 1.3.0

* Use tool versions as task inputs. This helps ensure tasks are executed again when a tool version changes
* No longer create the package installation marker inside `node_modules` for Yarn since the presence of `node_modules`
  depends on the Node Linker used with Yarn

## 1.2.0

* Added a `nodeDownloadBase` option to provide custom base URL for NodeJS download
* Added support for Apple silicon versions of NodeJS
* Eliminated the need to depend on `project.afterEvaluate`

## 1.1.0

* Cleaned up some documentation
* Internal (non-breaking) refactorings and test improvements

## 1.0.0

Initial open source release. No longer available on plugins.gradle.org
