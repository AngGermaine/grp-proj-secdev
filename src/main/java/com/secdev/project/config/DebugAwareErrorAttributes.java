package com.secdev.project.config;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@Component
public class DebugAwareErrorAttributes extends DefaultErrorAttributes {

    private final Environment environment;

    public DebugAwareErrorAttributes(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> attributes = super.getErrorAttributes(webRequest, options);

        boolean debugEnabled = Boolean.parseBoolean(environment.getProperty("debug", "false"));
        attributes.put("debugEnabled", debugEnabled);

        if (debugEnabled) {
            Throwable error = getError(webRequest);
            if (error != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                error.printStackTrace(pw);
                attributes.put("trace", sw.toString());
            }
        }

        return attributes;
    }
}
