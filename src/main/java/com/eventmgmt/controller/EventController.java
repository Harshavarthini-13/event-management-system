package com.eventmgmt.controller;

import com.eventmgmt.dto.request.CreateEventRequest;
import com.eventmgmt.dto.request.UpdateEventRequest;
import com.eventmgmt.dto.response.ApiResponse;
import com.eventmgmt.dto.response.EventResponse;
import com.eventmgmt.enums.EventStatus;
import com.eventmgmt.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Event management endpoints.
 *
 * GET    /api/events                  — all events (PUBLIC)
 * GET    /api/events/upcoming         — upcoming events (PUBLIC)
 * GET    /api/events/status/{status}  — events by status (PUBLIC)
 * GET    /api/events/search?keyword=  — search events (PUBLIC)
 * GET    /api/events/{id}             — single event (PUBLIC)
 * GET    /api/events/organizer/{id}   — events by organizer (ADMIN/ORGANIZER)
 * GET    /api/events/stats            — dashboard stats (ADMIN)
 * POST   /api/events                  — create event (ADMIN)
 * PUT    /api/events/{id}             — update event (ADMIN)
 * DELETE /api/events/{id}             — delete event (ADMIN)
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success("Events fetched", events));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcomingEvents() {
        List<EventResponse> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(ApiResponse.success("Upcoming events fetched", events));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEventsByStatus(
            @PathVariable EventStatus status) {

        List<EventResponse> events = eventService.getEventsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Events fetched", events));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EventResponse>>> searchEvents(
            @RequestParam String keyword) {

        List<EventResponse> events = eventService.searchEvents(keyword);
        return ResponseEntity.ok(ApiResponse.success("Search results", events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @PathVariable Long id) {

        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success("Event fetched", event));
    }

    @GetMapping("/organizer/{organizerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEventsByOrganizer(
            @PathVariable Long organizerId) {

        List<EventResponse> events = eventService.getEventsByOrganizer(organizerId);
        return ResponseEntity.ok(ApiResponse.success("Events fetched", events));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getEventStats() {
        Map<String, Long> stats = eventService.getEventStats();
        return ResponseEntity.ok(ApiResponse.success("Stats fetched", stats));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request) {

        EventResponse event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", event));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request) {

        EventResponse event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", event));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully"));
    }
}