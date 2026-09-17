package com.eventmgmt.dto.response;

import com.eventmgmt.entity.Certificate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Certificate data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {

    private Long id;
    private Long registrationId;
    private String studentName;
    private String eventTitle;
    private LocalDate eventDate;
    private String certificateNumber;
    private String pdfUrl;
    private LocalDateTime issuedAt;

    public static CertificateResponse fromEntity(Certificate certificate) {
        return CertificateResponse.builder()
                .id(certificate.getId())
                .registrationId(certificate.getRegistration().getId())
                .studentName(certificate.getStudentName())
                .eventTitle(certificate.getEventTitle())
                .eventDate(certificate.getEventDate())
                .certificateNumber(certificate.getCertificateNumber())
                .pdfUrl(certificate.getPdfUrl())
                .issuedAt(certificate.getIssuedAt())
                .build();
    }
}