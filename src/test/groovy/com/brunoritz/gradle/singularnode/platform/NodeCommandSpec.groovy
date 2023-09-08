package com.brunoritz.gradle.singularnode.platform

import com.brunoritz.gradle.singularnode.platform.layout.InstallationLayout
import io.vavr.collection.HashMap
import io.vavr.collection.List
import org.gradle.api.Action
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import spock.lang.Specification

class NodeCommandSpec
	extends Specification
{
	private static final NODE_BIN_DIR = new File('nodejs/node/bin')
	private static final NODE_EXECUTABLE = new File('nodejs/node/bin/node')

	def 'It shall use the NodeJS command indicated by the installation layout'()
	{
		given:
			def processes = Mock(ExecOperations)
			def workingDirectory = new File('working-dir')
			def layout = Mock(InstallationLayout)
			def execSpec = Mock(ExecSpec)
			def nodeExecutable = new File('nodejs/node/bin/node')
			def command = new NodeCommand(processes, workingDirectory, layout)

		when:
			command.execute()

		then:
			1 * processes.exec { _ as Action } >> { Action action -> action.execute(execSpec) }
			1 * layout.pathOfNodeExecutable() >> NODE_EXECUTABLE
			1 * layout.nodeJsBinDirectory() >> NODE_BIN_DIR
			1 * execSpec.setExecutable(nodeExecutable.absolutePath)
	}

	def 'It shall give precedence to newer envrionment variables over the existing ones'()
	{
		given:
			def processes = Mock(ExecOperations)
			def workingDirectory = new File('working-dir')
			def layout = Mock(InstallationLayout)
			def execSpec = Mock(ExecSpec)
			def command = new NodeCommand(processes, workingDirectory, layout)
				.withEnvironment(HashMap.of('OVERRIDE', 'old-value'))

		when:
			command
				.withEnvironment(HashMap.of('OVERRIDE', 'new-value'))
				.execute()

		then:
			1 * processes.exec { _ as Action } >> { Action action -> action.execute(execSpec) }
			1 * layout.pathOfNodeExecutable() >> NODE_EXECUTABLE
			1 * layout.nodeJsBinDirectory() >> NODE_BIN_DIR
			1 * execSpec.environment(_) >> { Map<String, String> envVars ->
				assert envVars['OVERRIDE'] == 'new-value'

				return execSpec
			}
	}

	def 'It shall be possible to append arguments to the exiting ones'()
	{
		given:
			def processes = Mock(ExecOperations)
			def workingDirectory = new File('working-dir')
			def layout = Mock(InstallationLayout)
			def execSpec = Mock(ExecSpec)
			def command = new NodeCommand(processes, workingDirectory, layout)

		when:
			command
				.args('--foo=bar')
				.args('--bar=baz')
				.args(List.ofAll([
					'--something=nothing',
					'--more=less'
				]))
				.execute()

		then:
			1 * processes.exec { _ as Action } >> { Action action -> action.execute(execSpec) }

		then:
			1 * layout.pathOfNodeExecutable() >> NODE_EXECUTABLE
			1 * layout.nodeJsBinDirectory() >> NODE_BIN_DIR
			1 * execSpec.setArgs([
				'--foo=bar',
				'--bar=baz',
				'--something=nothing',
				'--more=less'
			])
	}

	def 'It shall be possible to append environment variables'()
	{
		given:
			def processes = Mock(ExecOperations)
			def workingDirectory = new File('working-dir')
			def layout = Mock(InstallationLayout)
			def execSpec = Mock(ExecSpec)
			def command = new NodeCommand(processes, workingDirectory, layout)

		when:
			command
				.withEnvironment(HashMap.ofAll([
					FOO: 'BAR',
					BAR: 'BAZ'
				]))
				.withEnvironment(HashMap.of('SOMETHING', 'NOTHING'))
				.execute()

		then:
			1 * processes.exec { _ as Action } >> { Action action -> action.execute(execSpec) }

		then:
			1 * layout.pathOfNodeExecutable() >> NODE_EXECUTABLE
			1 * layout.nodeJsBinDirectory() >> NODE_BIN_DIR
			1 * execSpec.environment(_) >> { Map<String, String> envVars ->
				assert envVars['FOO'] == 'BAR'
				assert envVars['BAR'] == 'BAZ'
				assert envVars['SOMETHING'] == 'NOTHING'

				return execSpec
			}
	}

	def 'It shall prepend PATH with the binary directory of the managed NodeJS installation'()
	{
		given:
			def processes = Mock(ExecOperations)
			def workingDirectory = new File('working-dir')
			def layout = Mock(InstallationLayout)
			def execSpec = Mock(ExecSpec)
			def command = new NodeCommand(processes, workingDirectory, layout)

		when:
			command.execute()

		then:
			1 * processes.exec { _ as Action } >> { Action action -> action.execute(execSpec) }

		then:
			1 * layout.pathOfNodeExecutable() >> NODE_EXECUTABLE
			1 * layout.nodeJsBinDirectory() >> NODE_BIN_DIR
			1 * execSpec.environment(_) >> { Map<String, String> envVars ->
				assert envVars['PATH'].startsWith("${NODE_BIN_DIR.absolutePath}${File.pathSeparator}")

				return execSpec
			}
	}
}
