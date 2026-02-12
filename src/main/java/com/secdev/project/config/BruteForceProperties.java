package com.secdev.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.bruteforce")
public class BruteForceProperties {

    private int maxEmailAttempts;
    private int maxIpAttempts;
    private int windowMinutes;

    public int getMaxEmailAttempts() {
        return maxEmailAttempts;
    }

    public void setMaxEmailAttempts(int maxEmailAttempts) {
        this.maxEmailAttempts = maxEmailAttempts;
    }

    public int getMaxIpAttempts() {
        return maxIpAttempts;
    }

    public void setMaxIpAttempts(int maxIpAttempts) {
        this.maxIpAttempts = maxIpAttempts;
    }

    public int getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(int windowMinutes) {
        this.windowMinutes = windowMinutes;
    }
}
