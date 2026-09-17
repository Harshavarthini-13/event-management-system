package com.eventmgmt.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for creating a new event.
 * Used by: POST /api/events
 * Role: ADMIN only
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotNull(message = "Organizer ID is required")
    private Long organizerId;

    @NotBlank(message = "Event title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Event date is required")
    @FutureOrPresent(message = "Event date must be today or in the future")
    private LocalDate eventDate;

    /**
     * Optional end date for multi-day events.
     * If not provided, the event is treated as a single-day event.
     */
    @FutureOrPresent(message = "End date must be today or in the future")
    private LocalDate endDate;

    @NotNull(message = "Event time is required")
    private LocalTime eventTime;

    @NotBlank(message = "Venue is required")
    @Size(max = 255, message = "Venue cannot exceed 255 characters")
    private String venue;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity cannot exceed 10000")
    private Integer capacity;
}