package com.jimisola.demo.gradle.plugins.observability;

import static com.jimisola.demo.gradle.plugins.observability.Versions.ARCONIA_VERSION;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;


/**
 * Convention plugin for Arconia observability configuration.
 * Versions are managed in PluginVersions.java and updated by Renovate.
 * 
 * Adds:
 * - Arconia BOM for dependency management
 * - Arconia OpenTelemetry Spring Boot Starter (uses Micrometer instrumentation, no Java Agent)
 * - Arconia Dev Services LGTM (testAndDevelopmentOnly)
 */
public class ObservabilityConventionPlugin implements Plugin<Project> {
    
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        
        project.getDependencies().add("implementation",
            project.getDependencies().platform("io.arconia:arconia-bom:" + ARCONIA_VERSION));
        
        project.getDependencies().add("implementation", 
            "io.arconia:arconia-opentelemetry-spring-boot-starter");
        
        project.getDependencies().add("testAndDevelopmentOnly", 
            "io.arconia:arconia-dev-services-lgtm:" + ARCONIA_VERSION);
    }
}