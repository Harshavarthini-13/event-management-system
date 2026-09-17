package com.eventmgmt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a student tries to register for an event
 * they are already registered for.
 * Maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRegistrationException extends RuntimeException {
    public DuplicateRegistrationException(String message) {
        super(message);
    }
}