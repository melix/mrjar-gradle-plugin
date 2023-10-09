pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "gradle-mrjar-plugin"

include("plugin")
include("docs")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

gradle.beforeProject {
    extra.set("supportedGradleVersions", setOf(
            "", // current,
            "7.1.1",
            "7.2",
            "7.3",
	    "8.0",
	    "8.3",
	    "8.4"
    ))
}
gradle.afterProject {
    version = project.extensions.findByType<VersionCatalogsExtension>()!!.find("libs")
            .flatMap { it.findVersion("mrjar") }
            .map { it.requiredVersion }
            .orElseThrow()
}
