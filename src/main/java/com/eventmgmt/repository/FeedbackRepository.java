package com.eventmgmt.repository;

import com.eventmgmt.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Feedback entity.
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Check if student already submitted feedback for an event
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    // Get student's feedback for a specific event
    Optional<Feedback> findByUserIdAndEventId(Long userId, Long eventId);

    // Get all feedback for an event (admin view)
    List<Feedback> findByEventId(Long eventId);

    // Get all feedback submitted by a student
    List<Feedback> findByUserId(Long userId);

    // Calculate average rating for an event
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event.id = :eventId")
    Double findAverageRatingByEventId(@Param("eventId") Long eventId);

    // Count feedback submissions for an event
    long countByEventId(Long eventId);

    // Get feedback with user details for admin view
    @Query("SELECT f FROM Feedback f " +
           "JOIN FETCH f.user " +
           "JOIN FETCH f.event " +
           "WHERE f.event.id = :eventId " +
           "ORDER BY f.submittedAt DESC")
    List<Feedback> findByEventIdWithDetails(@Param("eventId") Long eventId);

    // Get rating distribution for an event
    @Query("SELECT f.rating, COUNT(f) FROM Feedback f " +
           "WHERE f.event.id = :eventId " +
           "GROUP BY f.rating " +
           "ORDER BY f.rating")
    List<Object[]> findRatingDistributionByEventId(@Param("eventId") Long eventId);
}