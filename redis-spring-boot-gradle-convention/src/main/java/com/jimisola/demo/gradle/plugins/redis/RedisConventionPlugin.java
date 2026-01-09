package com.jimisola.demo.gradle.plugins.redis;

import static com.jimisola.demo.gradle.plugins.redis.Versions.ARCONIA_VERSION;
import static com.jimisola.demo.gradle.plugins.redis.Versions.TESTCONTAINERS_REDIS_VERSION;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

/**
 * Convention plugin for Redis and Arconia dev services configuration.
 * Versions are managed in Versions.java.
 * 
 * Adds:
 * - Spring Data Redis Starter
 * - Arconia Dev Services Redis (testAndDevelopmentOnly)
 * - Spring Data Redis Test Starter
 * - Testcontainers Redis
 * - Spring Boot Testcontainers support (testAndDevelopmentOnly)
 * - Testcontainers core (testAndDevelopmentOnly)   
 */
public class RedisConventionPlugin implements Plugin<Project> {
    
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        
        project.getDependencies().add("implementation", 
            "org.springframework.boot:spring-boot-starter-data-redis");
        
        project.getDependencies().add("testAndDevelopmentOnly", 
            "io.arconia:arconia-dev-services-redis:" + ARCONIA_VERSION);
        
        project.getDependencies().add("testImplementation", 
            "org.springframework.boot:spring-boot-starter-data-redis-test");
        
        project.getDependencies().add("testImplementation", 
            "com.redis:testcontainers-redis:" + TESTCONTAINERS_REDIS_VERSION);
        
        project.getDependencies().add("testAndDevelopmentOnly", 
            "org.springframework.boot:spring-boot-testcontainers");
        project.getDependencies().add("testAndDevelopmentOnly", 
            "org.testcontainers:testcontainers");
    }
}
