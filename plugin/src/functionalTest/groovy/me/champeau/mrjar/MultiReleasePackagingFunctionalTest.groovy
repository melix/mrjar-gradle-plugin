package me.champeau.mrjar

class MultiReleasePackagingFunctionalTest extends AbstractFunctionalTest {
    def "can package a multi-release jar"() {
        withSample 'basic'

        when:
        run 'jar'

        then:
        tasks {
            succeeded ':compileJava', ':compileJava11Java', ':jar'
        }

        verifyJar("build/libs/basic.jar") {
            isMultiRelease()
            hasFile 'META-INF/versions/11/demo/app/Version.class'
        }
    }
}
