package com.eventmgmt.service;

import com.eventmgmt.dto.request.FeedbackRequest;
import com.eventmgmt.dto.response.FeedbackResponse;
import com.eventmgmt.entity.Event;
import com.eventmgmt.entity.Feedback;
import com.eventmgmt.entity.User;
import com.eventmgmt.enums.RegistrationStatus;
import com.eventmgmt.exception.DuplicateRegistrationException;
import com.eventmgmt.exception.ResourceNotFoundException;
import com.eventmgmt.repository.FeedbackRepository;
import com.eventmgmt.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles feedback submission and retrieval.
 *
 * Business rules:
 * - Only students who ATTENDED can submit feedback
 * - One feedback per student per event
 * - Rating must be 1-5 (validated at DTO + DB level)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository     feedbackRepository;
    private final RegistrationRepository registrationRepository;
    private final EventService           eventService;
    private final UserService            userService;

    /**
     * Submit feedback for an event.
     * Only allowed if the student attended the event.
     *
     * @param request  eventId, rating, comment
     * @param userEmail logged-in student's email
     * @return FeedbackResponse
     */
    @Transactional
    public FeedbackResponse submitFeedback(FeedbackRequest request, String userEmail) {

        User  student = userService.getUserEntityByEmail(userEmail);
        Event event   = eventService.getEventEntityById(request.getEventId());

        // Rule 1: Student must have attended this event
        boolean hasAttended = registrationRepository
                .findByUserIdAndEventId(student.getId(), request.getEventId())
                .map(reg -> reg.getStatus() == RegistrationStatus.ATTENDED)
                .orElse(false);

        if (!hasAttended) {
            throw new IllegalArgumentException(
                    "You can only submit feedback for events you have attended"
            );
        }

        // Rule 2: No duplicate feedback
        if (feedbackRepository.existsByUserIdAndEventId(
                student.getId(), request.getEventId())) {
            throw new DuplicateRegistrationException(
                    "You have already submitted feedback for this event"
            );
        }

        Feedback feedback = Feedback.builder()
                .user(student)
                .event(event)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        feedback = feedbackRepository.save(feedback);
        log.info("Feedback submitted by '{}' for event '{}'. Rating: {}",
                userEmail, event.getTitle(), request.getRating());

        return FeedbackResponse.fromEntity(feedback);
    }

    /**
     * Get all feedback for an event (Admin view).
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByEvent(Long eventId) {
        return feedbackRepository.findByEventIdWithDetails(eventId)
                .stream()
                .map(FeedbackResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all feedback submitted by a student.
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getMyFeedback(String userEmail) {
        User student = userService.getUserEntityByEmail(userEmail);
        return feedbackRepository.findByUserId(student.getId())
                .stream()
                .map(FeedbackResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get event feedback summary with average rating and distribution.
     * Used by Admin dashboard.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEventFeedbackSummary(Long eventId) {
        Double avgRating = feedbackRepository.findAverageRatingByEventId(eventId);
        long   count     = feedbackRepository.countByEventId(eventId);

        List<Object[]> distribution =
                feedbackRepository.findRatingDistributionByEventId(eventId);

        Map<String, Long> ratingDist = new HashMap<>();
        for (Object[] row : distribution) {
            ratingDist.put(row[0].toString(), (Long) row[1]);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("eventId",           eventId);
        summary.put("totalFeedback",      count);
        summary.put("averageRating",      avgRating != null
                ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        summary.put("ratingDistribution", ratingDist);

        return summary;
    }
}