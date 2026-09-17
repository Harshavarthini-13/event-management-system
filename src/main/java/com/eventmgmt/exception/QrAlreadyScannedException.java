package com.eventmgmt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an organizer scans a QR code that has already been scanned.
 * Prevents duplicate attendance marking.
 * Maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class QrAlreadyScannedException extends RuntimeException {
    public QrAlreadyScannedException(String ticketId) {
        super("QR code has already been scanned for ticket: " + ticketId);
    }
}