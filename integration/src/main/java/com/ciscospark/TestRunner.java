package com.ciscospark;

import org.apache.log4j.Logger;
import org.junit.runner.JUnitCore;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.util.Set;
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

        final String[] tests = classes.stream()
                .map(BeanDefinition::getBeanClassName)
                .collect(Collectors.toList())
                .toArray(new String[]{});

        JUnitCore.main(tests);
    }
}
