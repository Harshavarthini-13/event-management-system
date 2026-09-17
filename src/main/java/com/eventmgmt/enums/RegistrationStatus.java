package com.eventmgmt.enums;

/**
 * States of a student's registration for an event.
 *
 * REGISTERED — Student has registered; QR ticket generated
 * CANCELLED  — Student cancelled before the event
 * ATTENDED   — QR was scanned; attendance confirmed
 */
public enum RegistrationStatus {
    REGISTERED,
    CANCELLED,
    ATTENDED
}