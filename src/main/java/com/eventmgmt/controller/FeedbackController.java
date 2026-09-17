package com.eventmgmt.controller;

import com.eventmgmt.dto.request.FeedbackRequest;
import com.eventmgmt.dto.response.ApiResponse;
import com.eventmgmt.dto.response.FeedbackResponse;
import com.eventmgmt.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feedback submission and viewing endpoints.
 *
 * POST /api/feedback                    — submit feedback (STUDENT)
 * GET  /api/feedback/me                 — my submitted feedback (STUDENT)
 * GET  /api/feedback/event/{eventId}    — all feedback for event (ADMIN)
 * GET  /api/feedback/event/{eventId}/summary — rating summary (ADMIN)
 */
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        FeedbackResponse response = feedbackService.submitFeedback(request, email);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted successfully", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getMyFeedback(
            Authentication authentication) {

        String email = authentication.getName();
        List<FeedbackResponse> feedback = feedbackService.getMyFeedback(email);

        return ResponseEntity.ok(ApiResponse.success("Feedback fetched", feedback));
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getFeedbackByEvent(
            @PathVariable Long eventId) {

        List<FeedbackResponse> feedback = feedbackService.getFeedbackByEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success("Feedback fetched", feedback));
    }

    @GetMapping("/event/{eventId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeedbackSummary(
            @PathVariable Long eventId) {

        Map<String, Object> summary = feedbackService.getEventFeedbackSummary(eventId);
        return ResponseEntity.ok(ApiResponse.success("Summary fetched", summary));
    }
}