package com.ciscospark;

import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO: move to cisco-spark-base
public class TestRunner {
    /**
     * A simple test runner that is used to run JUnit based integration tests that have been shaded into a jar.
     */
    public static void main(String[] args) throws Exception {
        LoggingSystem.get(ClassLoader.getSystemClassLoader()).setLogLevel(Logger.getRootLogger().getName(), LogLevel.INFO);

        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);

        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*IntegrationTest")));
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*IT")));
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile("IT.*")));

        final Set<BeanDefinition> classes = provider.findCandidateComponents("com.ciscospark");

        Function<BeanDefinition, Class> classFromBean = (BeanDefinition s) -> {
            String className = s.getBeanClassName();
            Class name = null;
            try {
                name = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                System.out.println("Error test class not found:" + className);

            } finally {
                return name;
            }
        };

        final Class[] tests = classes.stream()
                .map(classFromBean)
                .collect(Collectors.toList())
                .toArray(new Class[]{});

        JUnitCore junit = new JUnitCore();
        junit.addListener(new JUnitResultFormatterAsRunListener(new XMLJUnitResultFormatter()) {
            @Override
            public void testStarted(Description description) throws Exception {
                formatter.setOutput(new FileOutputStream(new File(".", "TEST-" + description.getDisplayName() + ".xml")));
                super.testStarted(description);
            }
        });

        junit.run(tests).wasSuccessful();
        System.exit(1);
    }
}
