package com.eventmgmt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores a student's rating and comment for an event they attended.
 *
 * Design decisions:
 * - UNIQUE on (user_id, event_id) — one feedback per student per event.
 * - rating is a TINYINT constrained to 1–5 by a DB CHECK constraint.
 *   We also validate at the DTO level with @Min/@Max.
 * - Only students who have ATTENDED (attendance record exists) should
 *   be allowed to submit feedback — enforced in FeedbackService.
 */
@Entity
@Table(
        name = "feedback",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_feedback_unique", columnNames = {"user_id", "event_id"})
        },
        indexes = {
                @Index(name = "idx_fb_event", columnList = "event_id"),
                @Index(name = "idx_fb_user",  columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_fb_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "event_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_fb_event")
    )
    private Event event;

    /**
     * Star rating from 1 (poor) to 5 (excellent).
     * Validated at DTO level and DB CHECK constraint.
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    // --------------------------------------------------------
    // JPA Lifecycle Hook
    // --------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }
}