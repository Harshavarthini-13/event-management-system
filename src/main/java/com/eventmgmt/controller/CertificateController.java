package com.eventmgmt.controller;

import com.eventmgmt.dto.response.ApiResponse;
import com.eventmgmt.dto.response.CertificateResponse;
import com.eventmgmt.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Certificate generation and verification endpoints.
 *
 * POST /api/certificates/registration/{registrationId} — generate cert (STUDENT)
 * GET  /api/certificates/me                              — my certificates (STUDENT)
 * GET  /api/certificates/verify/{certificateNumber}       — verify cert (PUBLIC)
 */
@RestController
@RequestMapping("/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping("/registration/{registrationId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<CertificateResponse>> generateCertificate(
            @PathVariable Long registrationId,
            Authentication authentication) {

        String email = authentication.getName();
        CertificateResponse response =
                certificateService.generateCertificate(registrationId, email);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Certificate generated successfully", response
                ));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getMyCertificates(
            Authentication authentication) {

        String email = authentication.getName();
        List<CertificateResponse> certificates =
                certificateService.getMyCertificates(email);

        return ResponseEntity.ok(
                ApiResponse.success("Certificates fetched", certificates)
        );
    }

    @GetMapping("/verify/{certificateNumber}")
    public ResponseEntity<ApiResponse<CertificateResponse>> verifyCertificate(
            @PathVariable String certificateNumber) {

        CertificateResponse response =
                certificateService.verifyCertificate(certificateNumber);

        return ResponseEntity.ok(
                ApiResponse.success("Certificate is valid", response)
        );
    }
}