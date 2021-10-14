pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "gradle-mrjar-plugin"

include("plugin")
include("docs")

enableFeaturePreview("VERSION_CATALOGS")

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
            "7.3-rc-1"
    ))
}
gradle.afterProject {
    version = project.extensions.findByType<VersionCatalogsExtension>()!!.find("libs")
            .flatMap { it.findVersion("mrjar") }
            .map { it.requiredVersion }
            .orElseThrow()
}
