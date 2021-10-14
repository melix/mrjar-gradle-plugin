package me.champeau.mrjar

class MultiReleaseApplicationFunctionalTest extends AbstractFunctionalTest {
    def "can execute application on different Java runtimes with expected outcome"() {
        withSample 'basic'

        when:
        run ':run'

        then:
        tasks {
            succeeded ':run'
        }

        outputContains 'Base version'

        when:
        run ':java11Run'

        then:
        tasks {
            succeeded ':java11Run'
        }

        outputContains 'Java 11 version'

    }
}
