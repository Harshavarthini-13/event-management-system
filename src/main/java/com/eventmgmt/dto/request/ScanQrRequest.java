package com.eventmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for scanning a QR code to mark attendance.
 * Used by: POST /api/attendance/scan
 * Role: ORGANIZER only
 *
 * The organizer's mobile/device scans the QR and sends:
 * - ticketId: the UUID decoded from the QR image
 * - eventId:  the event they are managing (prevents scanning
 *             tickets from other events)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanQrRequest {

    @NotBlank(message = "Ticket ID is required")
    private String ticketId;

    @NotNull(message = "Event ID is required")
    private Long eventId;
}