package com.secdev.project.config;

import com.secdev.project.service.exceptions.BadRequestException;
import com.secdev.project.service.exceptions.TooManyAttemptsException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequestException(BadRequestException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "register";
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    public String handleTooManyAttemptsException(TooManyAttemptsException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "login";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintViolationException(ConstraintViolationException ex, Model model) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Validation failed");
        model.addAttribute("error", message);
        return "register";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        logger.error("Unhandled exception", ex);

        model.addAttribute("error", "An unexpected error occurred. Please try again.");

        if (isDebugEnabled()) {
            model.addAttribute("debugEnabled", true);
            model.addAttribute("stackTrace", getStackTrace(ex));
        } else {
            model.addAttribute("debugEnabled", false);
        }

        return "error";
    }

    private boolean isDebugEnabled() {
        return Boolean.parseBoolean(environment.getProperty("debug", "false"));
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
