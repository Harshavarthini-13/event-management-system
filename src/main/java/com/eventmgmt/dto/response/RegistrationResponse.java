package com.eventmgmt.dto.response;

import com.eventmgmt.entity.Registration;
import com.eventmgmt.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for Registration data.
 * Includes QR code URL for the student to download their ticket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long eventId;
    private String eventTitle;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String eventVenue;
    private String ticketId;
    private String qrCodeUrl;
    private RegistrationStatus status;
    private LocalDateTime registeredAt;

    public static RegistrationResponse fromEntity(Registration registration) {
        return RegistrationResponse.builder()
                .id(registration.getId())
                .userId(registration.getUser().getId())
                .userName(registration.getUser().getName())
                .userEmail(registration.getUser().getEmail())
                .eventId(registration.getEvent().getId())
                .eventTitle(registration.getEvent().getTitle())
                .eventDate(registration.getEvent().getEventDate())
                .eventTime(registration.getEvent().getEventTime())
                .eventVenue(registration.getEvent().getVenue())
                .ticketId(registration.getTicketId())
                .qrCodeUrl(registration.getQrCodeUrl())
                .status(registration.getStatus())
                .registeredAt(registration.getRegisteredAt())
                .build();
    }
}