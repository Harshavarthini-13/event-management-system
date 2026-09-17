package com.eventmgmt.service;

import com.eventmgmt.dto.response.CertificateResponse;
import com.eventmgmt.entity.Certificate;
import com.eventmgmt.entity.Registration;
import com.eventmgmt.enums.RegistrationStatus;
import com.eventmgmt.exception.ResourceNotFoundException;
import com.eventmgmt.repository.CertificateRepository;
import com.eventmgmt.repository.RegistrationRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository  certificateRepository;
    private final RegistrationRepository registrationRepository;
    private final UserService            userService;

    @Value("${app.file.certificate-dir}")
    private String certificateDir;

    @Value("${app.file.base-url}")
    private String baseUrl;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    @Transactional
    public CertificateResponse generateCertificate(Long registrationId,
                                                   String userEmail) {

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration", "id", registrationId
                ));

        if (!registration.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException(
                    "You can only download your own certificate"
            );
        }

        if (registration.getStatus() != RegistrationStatus.ATTENDED) {
            throw new IllegalArgumentException(
                    "Certificate is only available after attendance is confirmed. " +
                            "Current status: " + registration.getStatus()
            );
        }

        // Certificate can only be generated after the event has fully ended
        if (!registration.getEvent().hasEnded()) {
            throw new IllegalArgumentException(
                    "Certificate will be available after the event ends on " +
                            registration.getEvent().getEffectiveEndDate().format(DATE_FORMATTER)
            );
        }

        if (certificateRepository.existsByRegistrationId(registrationId)) {
            Certificate existing = certificateRepository
                    .findByRegistrationId(registrationId)
                    .orElseThrow();
            log.info("Returning existing certificate: {}", existing.getCertificateNumber());
            return CertificateResponse.fromEntity(existing);
        }

        String certNumber = generateCertificateNumber();
        String pdfUrl = generatePdf(registration, certNumber);

        Certificate certificate = Certificate.builder()
                .registration(registration)
                .certificateNumber(certNumber)
                .pdfUrl(pdfUrl)
                .build();

        certificate = certificateRepository.save(certificate);
        log.info("Certificate generated: {} for student '{}'",
                certNumber, registration.getUser().getEmail());

        return CertificateResponse.fromEntity(certificate);
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getMyCertificates(String userEmail) {
        Long userId = userService.getUserEntityByEmail(userEmail).getId();
        return certificateRepository.findByUserId(userId)
                .stream()
                .map(CertificateResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CertificateResponse verifyCertificate(String certificateNumber) {
        Certificate certificate = certificateRepository
                .findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate", "number", certificateNumber
                ));
        return CertificateResponse.fromEntity(certificate);
    }

    private String generateCertificateNumber() {
        int year = java.time.LocalDate.now().getYear();
        long count = certificateRepository.count() + 1;
        return String.format("CERT-%d-%06d", year, count);
    }

    private String generatePdf(Registration registration,
                               String certificateNumber) {
        try {
            Path dirPath = Paths.get(certificateDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String fileName = certificateNumber + ".pdf";
            Path filePath = dirPath.resolve(fileName);

            // A4 landscape with tight margins
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 20, 20);
            PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
            document.open();

            // ── Fonts ──────────────────────────────────────────────
            Font titleFont = new Font(Font.HELVETICA, 36, Font.BOLD,
                    new Color(0x1A, 0x56, 0xDB));
            Font subtitleFont = new Font(Font.HELVETICA, 20, Font.ITALIC,
                    new Color(0x6B, 0x72, 0x80));
            Font bodyFont = new Font(Font.HELVETICA, 16, Font.NORMAL,
                    new Color(0x11, 0x18, 0x27));
            Font nameFont = new Font(Font.HELVETICA, 28, Font.BOLD,
                    new Color(0x10, 0x72, 0x48));
            Font eventFont = new Font(Font.HELVETICA, 22, Font.BOLD,
                    new Color(0x1A, 0x56, 0xDB));
            Font certNoFont = new Font(Font.HELVETICA, 11, Font.NORMAL,
                    new Color(0x9C, 0xA3, 0xAF));

            // ── Top border line ────────────────────────────────────
            addBorderLine(document);

            addSpacing(document, 5);

            // ── Title ──────────────────────────────────────────────
            Paragraph title = new Paragraph("CERTIFICATE OF PARTICIPATION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            addSpacing(document, 4);

            Paragraph subtitle = new Paragraph(
                    "This is to proudly certify that", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            addSpacing(document, 8);

            // ── Student Name ───────────────────────────────────────
            Paragraph studentName = new Paragraph(
                    registration.getUser().getName().toUpperCase(), nameFont);
            studentName.setAlignment(Element.ALIGN_CENTER);
            document.add(studentName);

            addUnderline(document);

            addSpacing(document, 4);

            Paragraph participated = new Paragraph(
                    "has successfully participated in", bodyFont);
            participated.setAlignment(Element.ALIGN_CENTER);
            document.add(participated);

            addSpacing(document, 4);

            // ── Event Name ─────────────────────────────────────────
            Paragraph eventName = new Paragraph(
                    registration.getEvent().getTitle(), eventFont);
            eventName.setAlignment(Element.ALIGN_CENTER);
            document.add(eventName);

            addSpacing(document, 8);

            // ── Event Date ─────────────────────────────────────────
            String formattedDate = registration.getEvent()
                    .getEventDate().format(DATE_FORMATTER);
            Paragraph date = new Paragraph(
                    "Held on  " + formattedDate, bodyFont);
            date.setAlignment(Element.ALIGN_CENTER);
            document.add(date);

            addSpacing(document, 4);

            // ── Venue ──────────────────────────────────────────────
            Paragraph venue = new Paragraph(
                    "Venue: " + registration.getEvent().getVenue(), bodyFont);
            venue.setAlignment(Element.ALIGN_CENTER);
            document.add(venue);

            addSpacing(document, 8);

            // ── Certificate Number ─────────────────────────────────
            Paragraph certNo = new Paragraph(
                    "Certificate No: " + certificateNumber, certNoFont);
            certNo.setAlignment(Element.ALIGN_CENTER);
            document.add(certNo);

            addSpacing(document, 4);

            // ── Bottom border line ─────────────────────────────────
            addBorderLine(document);

            document.close();
            log.info("PDF generated at: {}", filePath.toAbsolutePath());

            return baseUrl + "/certificates/" + fileName;

        } catch (DocumentException | IOException e) {
            log.error("Failed to generate certificate PDF: {}", e.getMessage());
            throw new RuntimeException("Certificate generation failed: " + e.getMessage(), e);
        }
    }

    private void addSpacing(Document document, float height)
            throws DocumentException {
        document.add(new Paragraph(" ") {{
            setSpacingBefore(height / 2f);
            setSpacingAfter(height / 2f);
        }});
    }

    private void addBorderLine(Document document) throws DocumentException {
        Paragraph line = new Paragraph("_".repeat(90));
        line.setAlignment(Element.ALIGN_CENTER);
        Font lineFont = new Font(Font.HELVETICA, 10, Font.NORMAL,
                new Color(0x1A, 0x56, 0xDB));
        line.setFont(lineFont);
        document.add(line);
    }

    private void addUnderline(Document document) throws DocumentException {
        Paragraph underline = new Paragraph("─".repeat(40));
        underline.setAlignment(Element.ALIGN_CENTER);
        Font underlineFont = new Font(Font.HELVETICA, 14, Font.NORMAL,
                new Color(0x10, 0x72, 0x48));
        underline.setFont(underlineFont);
        document.add(underline);
    }
}