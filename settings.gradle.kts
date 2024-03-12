pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
	}

	plugins {
		id("com.gradle.plugin-publish").version("1.2.1")
		id("com.github.spotbugs").version("5.2.1")
		id("org.checkerframework").version("0.6.34")
		id("me.qoomon.git-versioning").version("6.4.2")
	}
}

plugins {
	id("com.gradle.enterprise").version("3.15")
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
	}
}

gradleEnterprise {
	buildScan {
		publishAlwaysIf(System.getenv("CI") != null)
		termsOfServiceUrl = "https://gradle.com/terms-of-service"
		termsOfServiceAgree = "yes"
	}
}

rootProject.name = "singlular-node"
