package com.brunoritz.gradle.singularnode.nodejs

import spock.lang.Specification
import spock.lang.Unroll

class NodeDependencyFactorySpec
	extends Specification
{
	@Unroll
	def 'It shall compute a platform dependent name of the NodeJS dependency'(
		String version,
		String osName,
		String osArch,
		String expected)
	{
		given:
			def systemProperties = new Properties()

			systemProperties.setProperty('os.name', osName)
			systemProperties.setProperty('os.arch', osArch)

		when:
			def result = NodeDependencyFactory.computeDependencyString(version, systemProperties)

		then:
			result.isDefined()
			result.get() == expected

		where:
			version  | osName     | osArch    | expected
			'1.2.3'  | 'Windows'  | ''        | 'org.nodejs:node:1.2.3:win-x64@zip'
			'1.2.3'  | 'Linux'    | ''        | 'org.nodejs:node:1.2.3:linux-x64@tar.gz'

			'15.0.0' | 'Mac OS X' | 'aarch64' | 'org.nodejs:node:15.0.0:darwin-x64@tar.gz'
			'15.0.0' | 'Mac OS X' | 'x64'     | 'org.nodejs:node:15.0.0:darwin-x64@tar.gz'

			'16.0.0' | 'Mac OS X' | 'aarch64' | 'org.nodejs:node:16.0.0:darwin-arm64@tar.gz'
			'16.0.0' | 'Mac OS X' | 'x64'     | 'org.nodejs:node:16.0.0:darwin-x64@tar.gz'

			'xx.0.0' | 'Mac OS X' | 'aarch64' | 'org.nodejs:node:xx.0.0:darwin-x64@tar.gz'
			'xx.0.0' | 'Mac OS X' | 'x64'     | 'org.nodejs:node:xx.0.0:darwin-x64@tar.gz'
	}

	def 'It shall return no archive for unsupported platforms'()
	{
		given:
			def systemProperties = new Properties()

			systemProperties.setProperty('os.name', 'MS DOS')

		when:
			def result = NodeDependencyFactory.computeDependencyString('1.2.3', systemProperties)

		then:
			result.isEmpty()
	}
}
