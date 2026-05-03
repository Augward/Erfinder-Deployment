package edu.uiowa.team7;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

// Core Framework Configuration
@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "erfinder")
public class Configuration implements WebMvcConfigurer {

    // Application Settings Fields
    private int maxAttemptsInPeriod;
    private int clearAttemptAfterMinutes;
    private int timeoutPeriodMinutes;

    // Token Lifecycle Metrics
    public static long TokenLife() {
        return Duration.ofMinutes(10).toMillis();
    }

    // Token Refresh Metrics
    public static long TokenNoRefresh() {
        return Duration.ofMinutes(10).toMillis();
    }

    // View Controller Registry
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/landing.html");
        registry.addViewController("/{spring:\\w+}").setViewName("forward:/");
        registry.addViewController("/**/{spring:\\w+}").setViewName("forward:/");
    }
}