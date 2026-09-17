package com.eventmgmt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a scanned QR code does not match any valid registration,
 * belongs to a different event, or the registration is cancelled.
 * Maps to HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidQrCodeException extends RuntimeException {
    public InvalidQrCodeException(String message) {
        super(message);
    }
}