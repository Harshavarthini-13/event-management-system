package com.eventmgmt.dto.response;

import com.eventmgmt.entity.Feedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Feedback data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long eventId;
    private String eventTitle;
    private Integer rating;
    private String comment;
    private LocalDateTime submittedAt;

    public static FeedbackResponse fromEntity(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .userId(feedback.getUser().getId())
                .userName(feedback.getUser().getName())
                .eventId(feedback.getEvent().getId())
                .eventTitle(feedback.getEvent().getTitle())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .submittedAt(feedback.getSubmittedAt())
                .build();
    }
}