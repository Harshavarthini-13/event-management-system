package com.eventmgmt.repository;

import com.eventmgmt.entity.Registration;
import com.eventmgmt.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Registration entity.
 */
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Find registration by unique ticket ID (used during QR scan)
    Optional<Registration> findByTicketId(String ticketId);

    // Check if student already registered for an event
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    // Get all registrations for a student
    List<Registration> findByUserId(Long userId);

    // Get all registrations for an event
    List<Registration> findByEventId(Long eventId);

    // Get registration by student + event (for duplicate check)
    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);

    // Count non-cancelled registrations for an event (capacity check)
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId " +
           "AND r.status != 'CANCELLED'")
    long countActiveRegistrations(@Param("eventId") Long eventId);

    // Count attended registrations for an event (dashboard stat)
    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    // Get all registrations for an event with user details
    @Query("SELECT r FROM Registration r JOIN FETCH r.user WHERE r.event.id = :eventId")
    List<Registration> findByEventIdWithUser(@Param("eventId") Long eventId);

    // Get student registrations with event details
    @Query("SELECT r FROM Registration r JOIN FETCH r.event WHERE r.user.id = :userId " +
           "ORDER BY r.registeredAt DESC")
    List<Registration> findByUserIdWithEvent(@Param("userId") Long userId);

    // Check if ticket exists and is valid (not cancelled)
    @Query("SELECT r FROM Registration r WHERE r.ticketId = :ticketId " +
           "AND r.status = 'REGISTERED'")
    Optional<Registration> findValidTicket(@Param("ticketId") String ticketId);
}