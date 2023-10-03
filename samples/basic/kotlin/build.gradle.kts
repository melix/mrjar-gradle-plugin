// tag::plugin_use[]
plugins {
// end::plugin_use[]
    `java-library`
// tag::plugin_use[]
    id("me.champeau.mrjar") version "{gradle-project-version}"
// end::plugin_use[]
    application
// tag::plugin_use[]
}
// end::plugin_use[]

application {
    mainClass.set("demo.app.Application")
}

// tag::declaring_versions[]
multiRelease {
    targetVersions(8, 11)
}
// end::declaring_versions[]

repositories {
    mavenCentral()
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.8.1")
        }
    }
}
