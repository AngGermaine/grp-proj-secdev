package com.secdev.project.service.exceptions;

public class TooManyAttemptsException extends RuntimeException {
    public TooManyAttemptsException(String message) { super(message); }
}
