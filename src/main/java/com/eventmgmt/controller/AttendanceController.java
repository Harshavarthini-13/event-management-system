package com.eventmgmt.controller;

import com.eventmgmt.dto.request.ScanQrRequest;
import com.eventmgmt.dto.response.ApiResponse;
import com.eventmgmt.dto.response.AttendanceDashboardResponse;
import com.eventmgmt.dto.response.AttendanceResponse;
import com.eventmgmt.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * QR scanning and attendance tracking endpoints.
 *
 * POST /api/attendance/scan              — scan QR, mark attendance (ORGANIZER)
 * GET  /api/attendance/dashboard/{eventId} — live dashboard stats (ADMIN/ORGANIZER)
 * GET  /api/attendance/event/{eventId}     — all attendance records (ADMIN/ORGANIZER)
 *
 * Real-time updates are also pushed via WebSocket to:
 * /topic/attendance/{eventId}
 */
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/scan")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> scanQrCode(
            @Valid @RequestBody ScanQrRequest request,
            Authentication authentication) {

        String organizerEmail = authentication.getName();
        AttendanceResponse response =
                attendanceService.scanQrCode(request, organizerEmail);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance marked successfully", response));
    }

    @GetMapping("/dashboard/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<AttendanceDashboardResponse>> getDashboard(
            @PathVariable Long eventId) {

        AttendanceDashboardResponse dashboard =
                attendanceService.getDashboard(eventId);

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard fetched", dashboard)
        );
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByEvent(
            @PathVariable Long eventId) {

        List<AttendanceResponse> attendance =
                attendanceService.getAttendanceByEvent(eventId);

        return ResponseEntity.ok(
                ApiResponse.success("Attendance records fetched", attendance)
        );
    }
}