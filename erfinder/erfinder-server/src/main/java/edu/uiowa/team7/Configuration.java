package edu.uiowa.team7;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "erfinder")
public class Configuration {

    // Fields
    private int maxAttemptsInPeriod;
    private int clearAttemptAfterMinutes;
    private int timeoutPeriodMinutes;

    // Token Metrics
    public static long TokenLife() { return Duration.ofMinutes(15).toMillis(); }

    public static long TokenNoRefresh() {
        return Duration.ofMinutes(10).toMillis();
    }
}
