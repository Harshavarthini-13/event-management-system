package com.eventmgmt.service;

import com.eventmgmt.dto.request.CreateEventRequest;
import com.eventmgmt.dto.request.UpdateEventRequest;
import com.eventmgmt.dto.response.EventResponse;
import com.eventmgmt.entity.Event;
import com.eventmgmt.entity.User;
import com.eventmgmt.enums.EventStatus;
import com.eventmgmt.enums.Role;
import com.eventmgmt.exception.ResourceNotFoundException;
import com.eventmgmt.repository.EventRepository;
import com.eventmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all event CRUD operations and business rules.
 *
 * Business Rules:
 * - Only ADMIN can create/update/delete events
 * - Organizer must be a user with ORGANIZER role
 * - Cannot delete an event that has registrations
 * - Capacity cannot be reduced below current registration count
 * - End date (if provided) cannot be before the event start date
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository  userRepository;

    /**
     * Create a new event.
     * Admin specifies which organizer manages it.
     */
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {

        // Validate organizer exists and has ORGANIZER role
        User organizer = userRepository.findById(request.getOrganizerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organizer", "id", request.getOrganizerId()
                ));

        if (organizer.getRole() != Role.ORGANIZER) {
            throw new IllegalArgumentException(
                    "User with id " + request.getOrganizerId() + " is not an ORGANIZER"
            );
        }

        // Validate end date is not before start date
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getEventDate())) {
            throw new IllegalArgumentException("End date cannot be before the event start date");
        }

        Event event = Event.builder()
                .organizer(organizer)
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .endDate(request.getEndDate())
                .eventTime(request.getEventTime())
                .venue(request.getVenue())
                .capacity(request.getCapacity())
                .status(EventStatus.UPCOMING)
                .build();

        event = eventRepository.save(event);
        log.info("Event created: '{}' by organizer: {}", event.getTitle(),
                organizer.getEmail());

        return EventResponse.fromEntity(event);
    }

    /**
     * Update an existing event.
     * Only non-null fields in the request are updated.
     */
    @Transactional
    public EventResponse updateEvent(Long eventId, UpdateEventRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        // Update organizer if provided
        if (request.getOrganizerId() != null) {
            User newOrganizer = userRepository.findById(request.getOrganizerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Organizer", "id", request.getOrganizerId()
                    ));
            if (newOrganizer.getRole() != Role.ORGANIZER) {
                throw new IllegalArgumentException(
                        "User " + request.getOrganizerId() + " is not an ORGANIZER"
                );
            }
            event.setOrganizer(newOrganizer);
        }

        // Update only non-null fields
        if (request.getTitle()       != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate()   != null) event.setEventDate(request.getEventDate());
        if (request.getEndDate()     != null) event.setEndDate(request.getEndDate());
        if (request.getEventTime()   != null) event.setEventTime(request.getEventTime());
        if (request.getVenue()       != null) event.setVenue(request.getVenue());
        if (request.getStatus()      != null) event.setStatus(request.getStatus());

        // Validate end date is not before start date after updates
        if (event.getEndDate() != null && event.getEndDate().isBefore(event.getEventDate())) {
            throw new IllegalArgumentException("End date cannot be before the event start date");
        }

        // Capacity cannot be reduced below registered count
        if (request.getCapacity() != null) {
            int currentRegistrations = event.getRegisteredCount();
            if (request.getCapacity() < currentRegistrations) {
                throw new IllegalArgumentException(
                        "Cannot reduce capacity below current registration count of "
                                + currentRegistrations
                );
            }
            event.setCapacity(request.getCapacity());
        }

        event = eventRepository.save(event);
        log.info("Event updated: '{}'", event.getTitle());
        return EventResponse.fromEntity(event);
    }

    /**
     * Delete an event.
     * Only allowed if no active registrations exist.
     */
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (event.getRegisteredCount() > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete event with existing registrations. " +
                            "Cancel the event instead."
            );
        }

        eventRepository.delete(event);
        log.info("Event deleted: '{}'", event.getTitle());
    }

    /**
     * Get event by ID.
     */
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        return EventResponse.fromEntity(event);
    }

    /**
     * Get all events — visible to everyone including guests.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAllWithOrganizer()
                .stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming events from today.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents() {
        return eventRepository
                .findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate.now())
                .stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get events by status.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByStatus(EventStatus status) {
        return eventRepository.findByStatusOrderByEventDateAsc(status)
                .stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get events assigned to a specific organizer.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId)
                .stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Search events by title keyword.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> searchEvents(String keyword) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword)
                .stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get event entity by ID — used internally by other services.
     */
    @Transactional(readOnly = true)
    public Event getEventEntityById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
    }

    /**
     * Admin dashboard stats.
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getEventStats() {
        java.util.Map<String, Long> stats = new java.util.LinkedHashMap<>();
        stats.put("total",     eventRepository.count());
        stats.put("upcoming",  eventRepository.countByStatus(EventStatus.UPCOMING));
        stats.put("ongoing",   eventRepository.countByStatus(EventStatus.ONGOING));
        stats.put("completed", eventRepository.countByStatus(EventStatus.COMPLETED));
        stats.put("cancelled", eventRepository.countByStatus(EventStatus.CANCELLED));
        return stats;
    }
}