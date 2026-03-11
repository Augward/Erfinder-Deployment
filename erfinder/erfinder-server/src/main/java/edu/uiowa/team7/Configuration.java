package edu.uiowa.team7;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "erfinder")
public class Configuration {
    private int maxAttemptsInPeriod;
    private int clearAttemptAfterMinutes;
    private int timeoutPeriodMinutes;

    // how long a token is valid
    public static long TokenLife() {
        return Duration.ofMinutes(15).toMillis(); // parameterize
    }

    // how long a token lasts without being automatically refreshed
    public static long TokenNoRefresh() {
        return Duration.ofMinutes(10).toMillis();
    }
}
