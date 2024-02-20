import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask

plugins {
	id("java-gradle-plugin")
	id("jvm-test-suite")
	id("org.checkerframework")
	id("groovy")

	id("com.gradle.plugin-publish")

	id("com.github.spotbugs")
	id("checkstyle")
	id("pmd")

	id("me.qoomon.git-versioning")

	id("idea")
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17

	consistentResolution {
		useCompileClasspathVersions()
	}
}

checkerFramework {
	checkers = listOf(
		"org.checkerframework.checker.nullness.NullnessChecker",
		"org.checkerframework.common.initializedfields.InitializedFieldsChecker",
		"org.checkerframework.checker.formatter.FormatterChecker",
		"org.checkerframework.common.value.ValueChecker"
	)
}

testing {
	suites {
		val test by getting(JvmTestSuite::class) {
			useSpock()
		}

		register<JvmTestSuite>("integrationTest") {
			useSpock()

			dependencies {
				implementation(project())
			}
		}

		register<JvmTestSuite>("functionalTest") {
			useSpock()
		}
	}
}

gradlePlugin {
	website = "https://github.com/birdflyer-lszo/gradle-singlular-node"
	vcsUrl = "https://github.com/birdflyer-lszo/gradle-singlular-node"

	plugins {
		create("singularNode") {
			id = "com.brunoritz.gradle.singular-node"
			implementationClass = "com.brunoritz.gradle.singularnode.SingularNodePlugin"

			displayName = "Singular NodeJS Installation Plugin"
			description = "Allows projects to use a single NodeJS distribution in a multi-module project"
			tags = listOf(
				"node",
				"npm",
				"yarn",
				"pnpm",
				"multimodule"
			)
		}
	}

	testSourceSets(
		sourceSets["integrationTest"],
		sourceSets["functionalTest"]
	)
}

configurations["integrationTestImplementation"].extendsFrom(configurations["testImplementation"])

dependencies {
	implementation("com.github.spotbugs:spotbugs-annotations:4.8.3")
	implementation("io.vavr:vavr:0.10.4")
	implementation("net.jcip:jcip-annotations:1.0")

	testImplementation("cglib:cglib-nodep:3.3.0")
	testImplementation("org.spockframework:spock-core")
}

spotbugs {
	excludeFilter = file("config/spotbugs-exclusions.xml")
	showStackTraces = false

	effort = Effort.MAX
}

tasks.named<SpotBugsTask>("spotbugsMain") {
	reports.create("xml")
}

tasks.named("spotbugsIntegrationTest") {
	enabled = false
}

tasks.named("spotbugsFunctionalTest") {
	enabled = false
}

pmd {
	toolVersion = "6.33.0"
	isConsoleOutput = true
	isIgnoreFailures = true

	ruleSets = listOf()
	reportsDir = layout.buildDirectory.dir("reports/pmd").get().asFile
	ruleSetFiles = files("${rootDir}/config/pmd-rules.xml")

}

group = "com.brunoritz.gradle"

gitVersioning.apply {
	rev {
		version = "\${describe.tag.version}-dev+\${commit.short}"
	}

	refs {
		tag("v(?<version>.*)") {
			version = "\${ref.version}"
		}
	}
}

idea {
	module {
		testSources.from(
			sourceSets["integrationTest"].java.srcDirs,
			sourceSets["functionalTest"].java.srcDirs
		)

		testResources.from(
			sourceSets["integrationTest"].resources.srcDirs,
			sourceSets["functionalTest"].resources.srcDirs
		)
	}
}
