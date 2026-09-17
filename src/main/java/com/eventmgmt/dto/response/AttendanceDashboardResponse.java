package com.eventmgmt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for the real-time attendance dashboard.
 * Sent via WebSocket to the organizer's screen after every scan.
 *
 * Shows:
 * - Total registered students
 * - Total checked in (scanned)
 * - Attendance percentage
 * - Remaining (not yet arrived)
 * - Recent scan list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDashboardResponse {

    private Long eventId;
    private String eventTitle;
    private Integer totalRegistered;
    private Integer totalCheckedIn;
    private Integer remaining;
    private Double attendancePercentage;
    private List<AttendanceResponse> recentScans;

    public static AttendanceDashboardResponse of(
            Long eventId,
            String eventTitle,
            int totalRegistered,
            int totalCheckedIn,
            List<AttendanceResponse> recentScans) {

        int remaining = totalRegistered - totalCheckedIn;
        double percentage = totalRegistered > 0
                ? Math.round(((double) totalCheckedIn / totalRegistered) * 10000.0) / 100.0
                : 0.0;

        return AttendanceDashboardResponse.builder()
                .eventId(eventId)
                .eventTitle(eventTitle)
                .totalRegistered(totalRegistered)
                .totalCheckedIn(totalCheckedIn)
                .remaining(remaining)
                .attendancePercentage(percentage)
                .recentScans(recentScans)
                .build();
    }
}