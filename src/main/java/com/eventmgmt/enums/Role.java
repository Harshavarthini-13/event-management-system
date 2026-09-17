package com.eventmgmt.enums;

/**
 * Defines the three roles in the system.
 * Spring Security uses these as GrantedAuthority prefixed with "ROLE_"
 * e.g.  ROLE_ADMIN, ROLE_ORGANIZER, ROLE_STUDENT
 */
public enum Role {
    ADMIN,
    ORGANIZER,
    STUDENT
}