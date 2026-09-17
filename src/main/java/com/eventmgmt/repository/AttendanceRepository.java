package com.eventmgmt.repository;

import com.eventmgmt.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Attendance entity.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Check if a registration has already been scanned
    boolean existsByRegistrationId(Long registrationId);

    // Find attendance record by registration ID
    Optional<Attendance> findByRegistrationId(Long registrationId);

    // Get all attendance records for an event (via registration join)
    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.registration r " +
           "JOIN FETCH r.user " +
           "WHERE r.event.id = :eventId AND a.isValid = true")
    List<Attendance> findByEventId(@Param("eventId") Long eventId);

    // Count valid attendance records for an event
    @Query("SELECT COUNT(a) FROM Attendance a " +
           "JOIN a.registration r " +
           "WHERE r.event.id = :eventId AND a.isValid = true")
    long countByEventId(@Param("eventId") Long eventId);

    // Get all scans performed by a specific organizer
    List<Attendance> findByScannedById(Long scannedById);

    // Get attendance with full details for dashboard
    @Query("SELECT a FROM Attendance a " +
           "JOIN FETCH a.registration r " +
           "JOIN FETCH r.user u " +
           "JOIN FETCH r.event e " +
           "WHERE e.id = :eventId " +
           "ORDER BY a.scannedAt DESC")
    List<Attendance> findByEventIdWithDetails(@Param("eventId") Long eventId);
}