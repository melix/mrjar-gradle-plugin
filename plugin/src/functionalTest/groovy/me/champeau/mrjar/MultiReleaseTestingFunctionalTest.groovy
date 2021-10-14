package me.champeau.mrjar

class MultiReleaseTestingFunctionalTest extends AbstractFunctionalTest {
    def "tests of the main source set are executed on each version"() {
        withSample 'basic'

        when:
        fails 'check', '--continue'

        then:
        tasks {
            succeeded ':test'
            failed ':java11Test'
        }

        outputContains 'MessageTest > versionMessage() FAILED'
        outputDoesNotContain 'FixedMessageTest > versionMessage() FAILED'
        outputDoesNotContain 'OverridenTest > versionMessage() FAILED'

    }
}
