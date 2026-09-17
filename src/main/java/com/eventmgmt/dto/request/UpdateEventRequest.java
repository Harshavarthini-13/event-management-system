package com.eventmgmt.dto.request;

import com.eventmgmt.enums.EventStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for updating an existing event.
 * All fields are optional — only non-null fields will be updated.
 * Used by: PUT /api/events/{id}
 * Role: ADMIN only
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {

    // Organizer can be reassigned
    private Long organizerId;

    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private LocalDate eventDate;

    /**
     * Optional end date for multi-day events.
     * If not provided, the event remains a single-day event.
     */
    private LocalDate endDate;

    private LocalTime eventTime;

    @Size(max = 255, message = "Venue cannot exceed 255 characters")
    private String venue;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity cannot exceed 10000")
    private Integer capacity;

    // Admin can manually update status
    private EventStatus status;
}