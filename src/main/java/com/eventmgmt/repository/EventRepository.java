package com.eventmgmt.repository;

import com.eventmgmt.entity.Event;
import com.eventmgmt.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Event entity.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Get all events by status (UPCOMING, ONGOING, COMPLETED, CANCELLED)
    List<Event> findByStatus(EventStatus status);

    // Get all events managed by a specific organizer
    List<Event> findByOrganizerId(Long organizerId);

    // Get upcoming events from today onwards
    List<Event> findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate date);

    // Search events by title keyword
    List<Event> findByTitleContainingIgnoreCase(String keyword);

    // Get events by status ordered by date
    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    // Count events by status (for admin dashboard)
    long countByStatus(EventStatus status);

    // Count events by organizer
    long countByOrganizerId(Long organizerId);

    // Get events happening today
    List<Event> findByEventDate(LocalDate date);

    // Admin dashboard: get all events with organizer details in one query
    @Query("SELECT e FROM Event e JOIN FETCH e.organizer ORDER BY e.eventDate DESC")
    List<Event> findAllWithOrganizer();

    // Check if organizer has any active events before deactivating them
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.organizer.id = :organizerId " +
           "AND e.status IN ('UPCOMING', 'ONGOING')")
    boolean hasActiveEvents(@Param("organizerId") Long organizerId);
}