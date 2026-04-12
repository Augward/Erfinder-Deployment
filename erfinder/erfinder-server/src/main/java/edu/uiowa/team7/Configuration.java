package edu.uiowa.team7;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "erfinder")
public class Configuration implements WebMvcConfigurer{

    // Fields
    private int maxAttemptsInPeriod;
    private int clearAttemptAfterMinutes;
    private int timeoutPeriodMinutes;

    // Token Metrics
    public static long TokenLife() { return Duration.ofMinutes(15).toMillis(); }

    public static long TokenNoRefresh() {
        return Duration.ofMinutes(10).toMillis();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map the root URL strictly to landing.html
        registry.addViewController("/").setViewName("forward:/landing.html");

        // Fallbacks for direct navigation
        registry.addViewController("/{spring:\\w+}").setViewName("forward:/");
        registry.addViewController("/**/{spring:\\w+}").setViewName("forward:/");
    }
}
