package com.eventmgmt.dto.response;

import com.eventmgmt.entity.Event;
import com.eventmgmt.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for Event data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private Long organizerId;
    private String organizerName;
    private String title;
    private String description;
    private LocalDate eventDate;
    private LocalDate endDate;
    private LocalTime eventTime;
    private String venue;
    private Integer capacity;
    private Integer registeredCount;
    private Integer availableSeats;
    private EventStatus status;
    private LocalDateTime createdAt;
    private boolean hasEnded;

    public static EventResponse fromEntity(Event event) {
        int registered = event.getRegisteredCount();
        return EventResponse.builder()
                .id(event.getId())
                .organizerId(event.getOrganizer().getId())
                .organizerName(event.getOrganizer().getName())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .endDate(event.getEndDate())
                .eventTime(event.getEventTime())
                .venue(event.getVenue())
                .capacity(event.getCapacity())
                .registeredCount(registered)
                .availableSeats(event.getCapacity() - registered)
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .hasEnded(event.hasEnded())
                .build();
    }
}