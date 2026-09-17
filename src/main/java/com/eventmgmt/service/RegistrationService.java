package com.eventmgmt.service;

import com.eventmgmt.dto.response.RegistrationResponse;
import com.eventmgmt.entity.Event;
import com.eventmgmt.entity.Registration;
import com.eventmgmt.entity.User;
import com.eventmgmt.enums.EventStatus;
import com.eventmgmt.enums.RegistrationStatus;
import com.eventmgmt.exception.DuplicateRegistrationException;
import com.eventmgmt.exception.ResourceNotFoundException;
import com.eventmgmt.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles student event registration and QR ticket generation.
 *
 * Registration flow:
 * 1. Validate event is UPCOMING or ONGOING
 * 2. Check student not already registered
 * 3. Check event has available capacity
 * 4. Generate unique UUID ticket ID
 * 5. Generate QR code image via QrCodeService
 * 6. Save registration with QR URL
 * 7. Return RegistrationResponse with QR URL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventService           eventService;
    private final UserService            userService;
    private final QrCodeService          qrCodeService;

    /**
     * Register a student for an event.
     *
     * @param eventId ID of the event to register for
     * @param userEmail email of the logged-in student
     * @return RegistrationResponse including QR code URL
     */
    @Transactional
    public RegistrationResponse registerForEvent(Long eventId, String userEmail) {

        // Load student and event
        User  student = userService.getUserEntityByEmail(userEmail);
        Event event   = eventService.getEventEntityById(eventId);

        // Rule 1: Event must be registerable
        if (event.getStatus() == EventStatus.COMPLETED ||
                event.getStatus() == EventStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Registration is closed for this event. Status: " + event.getStatus()
            );
        }

        // Rule 2: No duplicate registration
        if (registrationRepository.existsByUserIdAndEventId(student.getId(), eventId)) {
            throw new DuplicateRegistrationException(
                    "You are already registered for event: " + event.getTitle()
            );
        }

        // Rule 3: Check capacity
        long activeCount = registrationRepository.countActiveRegistrations(eventId);
        if (activeCount >= event.getCapacity()) {
            throw new IllegalArgumentException(
                    "Event is full. No available seats for: " + event.getTitle()
            );
        }

        // Generate unique ticket ID (UUID)
        String ticketId = UUID.randomUUID().toString();

        // Generate QR code image and get URL
        String qrCodeUrl = qrCodeService.generateQrCode(ticketId);

        // Save registration
        Registration registration = Registration.builder()
                .user(student)
                .event(event)
                .ticketId(ticketId)
                .qrCodeUrl(qrCodeUrl)
                .status(RegistrationStatus.REGISTERED)
                .build();

        registration = registrationRepository.save(registration);
        log.info("Student '{}' registered for event '{}'. Ticket: {}",
                student.getEmail(), event.getTitle(), ticketId);

        return RegistrationResponse.fromEntity(registration);
    }

    /**
     * Cancel a registration.
     * Student can only cancel their own registration.
     */
    @Transactional
    public RegistrationResponse cancelRegistration(Long registrationId, String userEmail) {

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration", "id", registrationId
                ));

        // Ensure student owns this registration
        if (!registration.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException(
                    "You can only cancel your own registration"
            );
        }

        // Cannot cancel if already attended
        if (registration.getStatus() == RegistrationStatus.ATTENDED) {
            throw new IllegalArgumentException(
                    "Cannot cancel a registration that has already been attended"
            );
        }

        // Soft delete QR code file
        qrCodeService.deleteQrCode(registration.getTicketId());

        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setQrCodeUrl(null);
        registration = registrationRepository.save(registration);

        log.info("Registration {} cancelled by student: {}",
                registrationId, userEmail);

        return RegistrationResponse.fromEntity(registration);
    }

    /**
     * Get all registrations for the logged-in student.
     */
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMyRegistrations(String userEmail) {
        User student = userService.getUserEntityByEmail(userEmail);
        return registrationRepository.findByUserIdWithEvent(student.getId())
                .stream()
                .map(RegistrationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific registration by ID.
     * Student can only view their own.
     */
    @Transactional(readOnly = true)
    public RegistrationResponse getRegistrationById(Long registrationId, String userEmail) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration", "id", registrationId
                ));

        // Ownership check (admin can bypass — enforced at controller level)
        if (!registration.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException(
                    "You can only view your own registration"
            );
        }

        return RegistrationResponse.fromEntity(registration);
    }

    /**
     * Get all registrations for an event (Admin/Organizer).
     */
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsByEvent(Long eventId) {
        return registrationRepository.findByEventIdWithUser(eventId)
                .stream()
                .map(RegistrationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}