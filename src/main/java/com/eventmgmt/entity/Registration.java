package com.eventmgmt.entity;

import com.eventmgmt.enums.RegistrationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a student's registration for an event.
 *
 * Key design decisions:
 * - ticket_id is a UUID generated at registration time — this is what
 *   gets encoded into the QR code. It is globally unique and non-guessable.
 * - The UNIQUE constraint on (user_id, event_id) prevents double registration.
 * - qr_code_url stores the path/URL to the saved QR image file.
 * - status transitions: REGISTERED → ATTENDED (on QR scan) or CANCELLED
 */
@Entity
@Table(
        name = "registrations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_registrations_ticket", columnNames = "ticket_id"),
                @UniqueConstraint(name = "uq_user_event",           columnNames = {"user_id", "event_id"})
        },
        indexes = {
                @Index(name = "idx_reg_user",   columnList = "user_id"),
                @Index(name = "idx_reg_event",  columnList = "event_id"),
                @Index(name = "idx_reg_ticket", columnList = "ticket_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_reg_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "event_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_reg_event")
    )
    private Event event;

    /**
     * Unique UUID used as QR payload.
     * Generated in RegistrationService before persisting.
     */
    @Column(name = "ticket_id", nullable = false, length = 36, updatable = false)
    private String ticketId;

    /**
     * Relative URL to the QR image: /qr-codes/{ticketId}.png
     * Populated after QR generation.
     */
    @Column(name = "qr_code_url", length = 512)
    private String qrCodeUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Back-reference to the attendance record (if scanned).
     * One registration produces at most one attendance record.
     */
    @OneToOne(mappedBy = "registration", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Attendance attendance;

    /**
     * Back-reference to certificate (issued after attendance confirmed).
     */
    @OneToOne(mappedBy = "registration", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Certificate certificate;

    // --------------------------------------------------------
    // JPA Lifecycle Hooks
    // --------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
        this.updatedAt    = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --------------------------------------------------------
    // Business helpers
    // --------------------------------------------------------

    public boolean isAttended() {
        return RegistrationStatus.ATTENDED.equals(this.status);
    }

    public boolean isCancelled() {
        return RegistrationStatus.CANCELLED.equals(this.status);
    }
}