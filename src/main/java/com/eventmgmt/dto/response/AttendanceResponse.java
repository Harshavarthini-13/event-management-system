package com.eventmgmt.dto.response;

import com.eventmgmt.entity.Attendance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Attendance data.
 * Returned after a successful QR scan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;
    private Long registrationId;
    private String ticketId;
    private Long userId;
    private String studentName;
    private String studentEmail;
    private Long eventId;
    private String eventTitle;
    private String scannedByName;
    private LocalDateTime scannedAt;
    private Boolean isValid;

    public static AttendanceResponse fromEntity(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .registrationId(attendance.getRegistration().getId())
                .ticketId(attendance.getRegistration().getTicketId())
                .userId(attendance.getRegistration().getUser().getId())
                .studentName(attendance.getRegistration().getUser().getName())
                .studentEmail(attendance.getRegistration().getUser().getEmail())
                .eventId(attendance.getRegistration().getEvent().getId())
                .eventTitle(attendance.getRegistration().getEvent().getTitle())
                .scannedByName(attendance.getScannedBy().getName())
                .scannedAt(attendance.getScannedAt())
                .isValid(attendance.getIsValid())
                .build();
    }
}