package com.eventmgmt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a PDF certificate issued to a student after attending an event.
 *
 * Design decisions:
 * - UNIQUE on registration_id — exactly one certificate per registration.
 * - certificate_number is a human-readable unique ID (e.g. CERT-2024-000001).
 *   Generated in CertificateService using a padded sequence.
 * - pdf_url is the relative path to the generated PDF file served statically.
 * - Certificate generation is only allowed when:
 *     (a) the student has an Attendance record for that registration, AND
 *     (b) the event status is COMPLETED or ONGOING.
 *   These rules are enforced in CertificateService.
 */
@Entity
@Table(
        name = "certificates",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_cert_registration", columnNames = "registration_id"),
                @UniqueConstraint(name = "uq_cert_number",       columnNames = "certificate_number")
        },
        indexes = {
                @Index(name = "idx_cert_reg", columnList = "registration_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The registration this certificate belongs to.
     * OneToOne — one registration produces at most one certificate.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "registration_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cert_registration")
    )
    private Registration registration;

    /**
     * Human-readable certificate number.
     * Format: CERT-{YEAR}-{6-digit-sequence}
     * Example: CERT-2024-000001
     */
    @Column(name = "certificate_number", nullable = false, length = 50)
    private String certificateNumber;

    /**
     * Relative URL to the PDF file.
     * Example: /certificates/CERT-2024-000001.pdf
     */
    @Column(name = "pdf_url", length = 512)
    private String pdfUrl;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    // --------------------------------------------------------
    // JPA Lifecycle Hook
    // --------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
    }

    // --------------------------------------------------------
    // Convenience accessors (avoids repeated navigation chains)
    // --------------------------------------------------------

    public String getStudentName() {
        return this.registration.getUser().getName();
    }

    public String getEventTitle() {
        return this.registration.getEvent().getTitle();
    }

    public java.time.LocalDate getEventDate() {
        return this.registration.getEvent().getEventDate();
    }
}