pluginManagement {
    includeBuild("build-logic")
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
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
            "7.3",
            "7.5",
            "7.6",
            "8.4-rc-3"
    ))
}
gradle.afterProject {
    version = project.extensions.findByType<VersionCatalogsExtension>()!!.find("libs")
            .flatMap { it.findVersion("mrjar") }
            .map { it.requiredVersion }
            .orElseThrow()
}
