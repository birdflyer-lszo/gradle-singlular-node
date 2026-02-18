package com.brunoritz.gradle.singularnode

import org.gradle.api.Project

final class MockNodeInstallation
{
	private MockNodeInstallation()
	{
		throw new UnsupportedOperationException()
	}

	static File simulateNodeInstallationInProject(Project project)
	{
		def source = new File(MockNodeInstallation.class.getResource('fake-node').file)
		def dest = project.file('nodejs')

		project.copy {
			from source
			into dest

			dirPermissions {
				user {
					write = true
					execute = true
				}
			}
			filePermissions {
				user {
					read = true
					execute = true
				}
			}
		}

		return dest
	}
}
