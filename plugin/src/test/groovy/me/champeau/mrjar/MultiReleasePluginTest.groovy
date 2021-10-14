package me.champeau.mrjar

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class MultiReleasePluginTest extends Specification {
    def "plugin creates extension"() {
        def project = ProjectBuilder.builder().build()

        when:
        project.plugins.apply "me.champeau.mrjar"

        then:
        project.extensions.findByType(MultiReleaseExtension)
    }
}
