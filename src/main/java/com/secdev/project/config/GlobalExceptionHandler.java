package com.secdev.project.config;

import com.secdev.project.service.exceptions.BadRequestException;
import com.secdev.project.service.exceptions.TooManyAttemptsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

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
        model.addAttribute("error", "An unexpected error occurred. Please try again.");
        return "register";
    }
}
