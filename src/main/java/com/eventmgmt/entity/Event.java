package com.eventmgmt.entity;

import com.eventmgmt.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an event created by an Admin and assigned to an Organizer.
 * An event has a capacity limit and tracks its own lifecycle via EventStatus.
 */
@Entity
@Table(
        name = "events",
        indexes = {
                @Index(name = "idx_events_organizer", columnList = "organizer_id"),
                @Index(name = "idx_events_status",    columnList = "status"),
                @Index(name = "idx_events_date",      columnList = "event_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The organizer assigned to manage this event.
     * RESTRICT on delete — you cannot delete a user who has events.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organizer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_events_organizer")
    )
    private User organizer;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    /**
     * Optional end date for multi-day events.
     * If null, the event is treated as a single-day event
     * and eventDate is used as both start and end.
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "event_time", nullable = false)
    private LocalTime eventTime;

    @Column(name = "venue", nullable = false, length = 255)
    private String venue;

    @Column(name = "capacity", nullable = false)
    @Builder.Default
    private Integer capacity = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --------------------------------------------------------
    // Relationships
    // --------------------------------------------------------

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Registration> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Feedback> feedbacks = new ArrayList<>();

    // --------------------------------------------------------
    // JPA Lifecycle Hooks
    // --------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --------------------------------------------------------
    // Business helper methods
    // --------------------------------------------------------

    /**
     * Checks whether the event still has open seats.
     * Called before allowing a new student registration.
     */
    public boolean hasAvailableCapacity() {
        long registered = this.registrations.stream()
                .filter(r -> r.getStatus() != com.eventmgmt.enums.RegistrationStatus.CANCELLED)
                .count();
        return registered < this.capacity;
    }

    /**
     * Returns the count of non-cancelled registrations.
     */
    public int getRegisteredCount() {
        return (int) this.registrations.stream()
                .filter(r -> r.getStatus() != com.eventmgmt.enums.RegistrationStatus.CANCELLED)
                .count();
    }

    /**
     * Returns the effective end date of the event.
     * For single-day events (endDate not set), this is the same as eventDate.
     * For multi-day events, this is the explicit endDate.
     */
    public LocalDate getEffectiveEndDate() {
        return this.endDate != null ? this.endDate : this.eventDate;
    }

    /**
     * Checks whether the event has fully ended (current date is after the
     * effective end date). Used to gate certificate generation.
     */
    public boolean hasEnded() {
        return LocalDate.now().isAfter(getEffectiveEndDate());
    }
}