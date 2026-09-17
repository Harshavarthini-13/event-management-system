package com.eventmgmt.service;

import com.eventmgmt.dto.request.ScanQrRequest;
import com.eventmgmt.dto.response.AttendanceDashboardResponse;
import com.eventmgmt.dto.response.AttendanceResponse;
import com.eventmgmt.entity.Attendance;
import com.eventmgmt.entity.Registration;
import com.eventmgmt.entity.User;
import com.eventmgmt.enums.RegistrationStatus;
import com.eventmgmt.exception.InvalidQrCodeException;
import com.eventmgmt.exception.QrAlreadyScannedException;
import com.eventmgmt.exception.ResourceNotFoundException;
import com.eventmgmt.repository.AttendanceRepository;
import com.eventmgmt.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles QR scanning and attendance management.
 *
 * QR Scan flow:
 * 1. Organizer scans student's QR image
 * 2. Device decodes QR → gets ticketId UUID
 * 3. POST /api/attendance/scan with {ticketId, eventId}
 * 4. AttendanceService validates:
 *    a. ticketId exists in registrations
 *    b. Registration belongs to correct event
 *    c. Registration is not cancelled
 *    d. QR not already scanned (duplicate prevention)
 * 5. Create Attendance record
 * 6. Update Registration status → ATTENDED
 * 7. Push dashboard update via WebSocket
 * 8. Return AttendanceResponse
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository   attendanceRepository;
    private final RegistrationRepository registrationRepository;
    private final UserService            userService;
    private final WebSocketService       webSocketService;

    /**
     * Process a QR scan and mark attendance.
     *
     * @param request    ticketId + eventId from scanner
     * @param scannerEmail email of the organizer performing the scan
     * @return AttendanceResponse with scan details
     */
    @Transactional
    public AttendanceResponse scanQrCode(ScanQrRequest request, String scannerEmail) {

        // Load the organizer performing the scan
        User organizer = userService.getUserEntityByEmail(scannerEmail);

        // Step 1: Find registration by ticketId
        Registration registration = registrationRepository
                .findByTicketId(request.getTicketId())
                .orElseThrow(() -> new InvalidQrCodeException(
                        "Invalid QR code. No registration found for ticket: "
                                + request.getTicketId()
                ));

        // Step 2: Verify ticket belongs to the correct event
        if (!registration.getEvent().getId().equals(request.getEventId())) {
            throw new InvalidQrCodeException(
                    "This QR code belongs to a different event. " +
                            "Expected event: " + request.getEventId() +
                            " but ticket is for event: " + registration.getEvent().getId()
            );
        }

        // Step 3: Check registration is not cancelled
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new InvalidQrCodeException(
                    "This registration has been cancelled and is no longer valid"
            );
        }

        // Step 4: Check for duplicate scan (DB UNIQUE constraint also protects this)
        if (attendanceRepository.existsByRegistrationId(registration.getId())) {
            throw new QrAlreadyScannedException(request.getTicketId());
        }

        // Step 5: Create attendance record
        Attendance attendance = Attendance.builder()
                .registration(registration)
                .scannedBy(organizer)
                .isValid(true)
                .build();

        attendance = attendanceRepository.save(attendance);

        // Step 6: Update registration status to ATTENDED
        registration.setStatus(RegistrationStatus.ATTENDED);
        registrationRepository.save(registration);

        log.info("Attendance marked for student '{}' at event '{}'. Scanned by: {}",
                registration.getUser().getEmail(),
                registration.getEvent().getTitle(),
                scannerEmail);

        // Step 7: Build response
        AttendanceResponse attendanceResponse = AttendanceResponse.fromEntity(attendance);

        // Step 8: Push real-time dashboard update via WebSocket
        pushDashboardUpdate(registration.getEvent().getId(),
                registration.getEvent().getTitle());

        return attendanceResponse;
    }

    /**
     * Get the attendance dashboard for an event.
     * Called by organizer to see current stats.
     */
    @Transactional(readOnly = true)
    public AttendanceDashboardResponse getDashboard(Long eventId) {

        // Total non-cancelled registrations
        long totalRegistered = registrationRepository.countActiveRegistrations(eventId);

        // Total valid scans
        long totalCheckedIn = attendanceRepository.countByEventId(eventId);

        // Recent scans for the live feed
        List<AttendanceResponse> recentScans = attendanceRepository
                .findByEventIdWithDetails(eventId)
                .stream()
                .limit(10)
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());

        // Try to get event title
        String eventTitle = recentScans.isEmpty() ? "Event #" + eventId
                : recentScans.get(0).getEventTitle();

        return AttendanceDashboardResponse.of(
                eventId,
                eventTitle,
                (int) totalRegistered,
                (int) totalCheckedIn,
                recentScans
        );
    }

    /**
     * Get all attendance records for an event.
     */
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByEvent(Long eventId) {
        return attendanceRepository.findByEventId(eventId)
                .stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Push updated dashboard to WebSocket subscribers.
     * Called after every successful scan.
     */
    private void pushDashboardUpdate(Long eventId, String eventTitle) {
        try {
            AttendanceDashboardResponse dashboard = getDashboard(eventId);
            webSocketService.sendAttendanceUpdate(eventId, dashboard);
        } catch (Exception e) {
            // WebSocket failure must never break the scan flow
            log.warn("Failed to push WebSocket update for event {}: {}",
                    eventId, e.getMessage());
        }
    }
}