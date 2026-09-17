package com.eventmgmt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Records a single QR scan event.
 *
 * Design decisions:
 * - UNIQUE on registration_id enforces "scan once" rule at the DB level,
 *   providing a hard guard even if the application layer fails.
 * - scanned_by links to the organizer who performed the scan.
 * - is_valid allows future soft-invalidation without deleting history.
 * - scanned_at is set by the DB default but also set in code for clarity.
 */
@Entity
@Table(
        name = "attendance",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_attendance_reg", columnNames = "registration_id")
        },
        indexes = {
                @Index(name = "idx_att_reg",     columnList = "registration_id"),
                @Index(name = "idx_att_scanner", columnList = "scanned_by")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The registration that was scanned.
     * OneToOne — each registration can be scanned exactly once.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "registration_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_att_registration")
    )
    private Registration registration;

    /**
     * The organizer (User with ORGANIZER role) who scanned the QR.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "scanned_by",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_att_scanner")
    )
    private User scannedBy;

    @Column(name = "scanned_at", nullable = false)
    private LocalDateTime scannedAt;

    /**
     * True by default. Can be set to false to invalidate a scan
     * (e.g. accidental scan or administrative correction).
     */
    @Column(name = "is_valid", nullable = false)
    @Builder.Default
    private Boolean isValid = true;

    // --------------------------------------------------------
    // JPA Lifecycle Hook
    // --------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.scannedAt = LocalDateTime.now();
    }
}