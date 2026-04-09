package com.secdev.project.util;

public class LoggingUtil {

    private LoggingUtil() {
        // Utility class
    }

    public static String sanitizeForLog(String input) {
        if (input == null) return null;
        return input
                .replaceAll("[\\r\\n\\t]", "_")
                .replaceAll("[^\\p{Print}]", "?");
    }
}