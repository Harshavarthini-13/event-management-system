package com.eventmgmt.service;

import com.eventmgmt.dto.response.AttendanceDashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Sends real-time attendance dashboard updates via WebSocket.
 *
 * After every QR scan, AttendanceService calls this service
 * to push the updated dashboard stats to all subscribers
 * of /topic/attendance/{eventId}.
 *
 * The organizer's browser (React) subscribes to this topic
 * and updates the attendance count display in real-time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast updated attendance dashboard to all subscribers
     * watching a specific event.
     *
     * @param eventId   the event ID
     * @param dashboard the updated dashboard data
     */
    public void sendAttendanceUpdate(Long eventId,
                                     AttendanceDashboardResponse dashboard) {
        String destination = "/topic/attendance/" + eventId;
        messagingTemplate.convertAndSend(destination, dashboard);
        log.debug("WebSocket update sent to {} — checked in: {}/{}",
                destination,
                dashboard.getTotalCheckedIn(),
                dashboard.getTotalRegistered());
    }
}