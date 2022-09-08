/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.champeau.mrjar;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.toolchain.JavaCompiler;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import javax.inject.Inject;

public abstract class MultiReleaseExtension {
    private final JavaPluginExtension javaPluginExtension;
    private final TaskContainer tasks;
    private final SourceSetContainer sourceSets;
    private final DependencyHandler dependencies;
    private final ObjectFactory objects;
    private final ConfigurationContainer configurations;
    private final JavaToolchainService javaToolchains;
    private final PluginManager pluginManager;
    private final ExtensionContainer extensions;

    @Inject
    public MultiReleaseExtension(JavaPluginExtension javaPluginExtension,
                                 SourceSetContainer sourceSets,
                                 ConfigurationContainer configurations,
                                 JavaToolchainService javaToolchains,
                                 TaskContainer tasks,
                                 DependencyHandler dependencies,
                                 ObjectFactory objectFactory,
                                 PluginManager pluginManager,
                                 ExtensionContainer extensions) {
        this.javaPluginExtension = javaPluginExtension;
        this.sourceSets = sourceSets;
        this.configurations = configurations;
        this.javaToolchains = javaToolchains;
        this.tasks = tasks;
        this.dependencies = dependencies;
        this.objects = objectFactory;
        this.pluginManager = pluginManager;
        this.extensions = extensions;
    }

    public void targetVersions(int defaultVersion, int... versions) {
        defaultLanguageVersion(defaultVersion);
        for (int version : versions) {
            addLanguageVersion(version);
        }
    }

    private void addLanguageVersion(int version) {
        String javaX = "java" + version;
        // First, let's create a source set for this language version
        SourceSet langSourceSet = sourceSets.create(javaX, srcSet -> srcSet.getJava().srcDir("src/main/" + javaX));
        SourceSet testSourceSet = sourceSets.create(javaX + "Test", srcSet -> srcSet.getJava().srcDir("src/test/" + javaX));
        SourceSet sharedSourceSet = sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSet sharedTestSourceSet = sourceSets.findByName(SourceSet.TEST_SOURCE_SET_NAME);

        // This is only necessary because in real life, we have dependencies between classes
        // and what you're likely to want to do, is to provide a JDK 9 specific class, which depends on common
        // classes of the main source set. In other words, you want to override some specific classes, but they
        // still have dependencies onto other classes.
        // We want to avoid recompiling all those classes, so we're just saying that the Java 9 specific classes
        // "depend on" the main ones.
        FileCollection mainClasses = objects.fileCollection().from(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput().getClassesDirs());
        dependencies.add(javaX + "Implementation", mainClasses);

        // then configure the compile task so that it uses the expected Gradle version
        Provider<JavaCompiler> targetCompiler = javaToolchains.compilerFor(spec -> spec.getLanguageVersion().convention(JavaLanguageVersion.of(version)));
        tasks.named(langSourceSet.getCompileJavaTaskName(), JavaCompile.class, task ->
                task.getJavaCompiler().convention(targetCompiler)
        );
        tasks.named(testSourceSet.getCompileJavaTaskName(), JavaCompile.class, task ->
                task.getJavaCompiler().convention(targetCompiler)
        );

        // let's make sure to create a "test" task
        Provider<JavaLauncher> targetLauncher = javaToolchains.launcherFor(spec -> spec.getLanguageVersion().convention(JavaLanguageVersion.of(version)));

        Configuration testImplementation = configurations.getByName(testSourceSet.getImplementationConfigurationName());
        testImplementation.extendsFrom(configurations.getByName(sharedTestSourceSet.getImplementationConfigurationName()));
        Configuration testCompileOnly = configurations.getByName(testSourceSet.getCompileOnlyConfigurationName());
        testCompileOnly.extendsFrom(configurations.getByName(sharedTestSourceSet.getCompileOnlyConfigurationName()));
        testCompileOnly.getDependencies().add(dependencies.create(langSourceSet.getOutput().getClassesDirs()));
        testCompileOnly.getDependencies().add(dependencies.create(sharedSourceSet.getOutput().getClassesDirs()));

        Configuration testRuntimeClasspath = configurations.getByName(testSourceSet.getRuntimeClasspathConfigurationName());
        // so here's the deal. MRjars are JARs! Which means that to execute tests, we need
        // the JAR on classpath, not just classes + resources as Gradle usually does
        testRuntimeClasspath.getAttributes()
                .attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.JAR));

        TaskProvider<Test> testTask = tasks.register("java" + version + "Test", Test.class, test -> {
            test.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
            test.getJavaLauncher().convention(targetLauncher);

            ConfigurableFileCollection testClassesDirs = objects.fileCollection();
            testClassesDirs.from(testSourceSet.getOutput());
            testClassesDirs.from(sharedTestSourceSet.getOutput());
            test.setTestClassesDirs(testClassesDirs);
            ConfigurableFileCollection classpath = objects.fileCollection();
            // must put the MRJar first on classpath
            classpath.from(tasks.named("jar"));
            // then we put the specific test sourceset tests, so that we can override
            // the shared versions
            classpath.from(testSourceSet.getOutput());

            // then we add the shared tests
            classpath.from(sharedTestSourceSet.getRuntimeClasspath());
            test.setClasspath(classpath);
        });

        tasks.named("check", task -> task.dependsOn(testTask));

        configureMrJar(version, langSourceSet);

        pluginManager.withPlugin("application", plugin -> {
            JavaApplication javaApp = extensions.getByType(JavaApplication.class);
            tasks.register("java" + version + "Run", JavaExec.class, run -> {
                        run.setGroup(ApplicationPlugin.APPLICATION_GROUP);
                        run.getJavaLauncher().convention(targetLauncher);
                        run.getMainClass().convention(javaApp.getMainClass());
                        run.setClasspath(langSourceSet.getRuntimeClasspath());
                    });
                }
        );
    }

    private void configureMrJar(int version, SourceSet languageSourceSet) {
        tasks.named("jar", Jar.class, jar -> {
            jar.into("META-INF/versions/" + version, s -> s.from(languageSourceSet.getOutput()));
            Attributes attributes = jar.getManifest().getAttributes();
            attributes.put("Multi-Release", "true");
        });
    }

    private void defaultLanguageVersion(int version) {
        javaPluginExtension.getToolchain().getLanguageVersion().convention(JavaLanguageVersion.of(version));
    }
}
