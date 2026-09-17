package com.eventmgmt.enums;

/**
 * Lifecycle states of an Event.
 *
 * UPCOMING   — Event is scheduled but not yet started
 * ONGOING    — Event is currently happening
 * COMPLETED  — Event has finished; certificates can be issued
 * CANCELLED  — Event was cancelled; no attendance or certificates
 */
public enum EventStatus {
    UPCOMING,
    ONGOING,
    COMPLETED,
    CANCELLED
}