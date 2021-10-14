plugins {
    `java-gradle-plugin`
    `java-test-fixtures`
    groovy
    id("com.gradle.plugin-publish") version "0.16.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    testFixturesApi(libs.groovy)
    testFixturesApi(libs.test.spock)
    testFixturesImplementation(gradleTestKit())
}

gradlePlugin {
    val mrjar by plugins.creating {
        id = "me.champeau.mrjar"
        implementationClass = "me.champeau.mrjar.MultiReleaseJarPlugin"
    }
}

pluginBundle {
    website = "https://melix.github.io/gradle-mrjar-plugin/"
    vcsUrl = "https://github.com/melix/gradle-mrjar-plugin"
    description = "Adds support for building multi-release jars with Gradle"
    tags = listOf("mrjar", "multi-release")

    plugins {
        named("mrjar") {
            displayName = "Gradle Multi-Release JAR plugin"
        }
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

val supportedGradleVersions: Set<String> by extra

val groovyDslFunctionalTests by tasks.registering
val kotlinDslFunctionalTests by tasks.registering

supportedGradleVersions.forEach { gradleVersion ->
    listOf("groovy", "kotlin").forEach { dsl ->
        val taskName = if (gradleVersion == "") {
            if (dsl == "groovy") {
                "functionalTest"
            } else {
                "kotlinDslFunctionalTest"
            }
        } else {
            val suffix = if (dsl == "groovy") {
                "FunctionalTest"
            } else {
                "kotlinDslFunctionalTest"
            }
            "gradle${gradleVersion.replace('.', '_')}$suffix"
        }

        val functionalTest = tasks.register<Test>(taskName) {
            inputs.files("../samples")
            testClassesDirs = functionalTestSourceSet.output.classesDirs
            classpath = functionalTestSourceSet.runtimeClasspath
            systemProperty("gradleVersion", gradleVersion)
            systemProperty("dsl", dsl)
        }

        tasks.check {
            dependsOn(functionalTest)
        }

        if (dsl == "groovy") {
            groovyDslFunctionalTests.configure { dependsOn(functionalTest) }
        } else {
            kotlinDslFunctionalTests.configure { dependsOn(functionalTest) }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
