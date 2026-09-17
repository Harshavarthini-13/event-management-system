package com.eventmgmt.controller;

import com.eventmgmt.dto.response.ApiResponse;
import com.eventmgmt.dto.response.RegistrationResponse;
import com.eventmgmt.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Student event registration endpoints.
 *
 * POST   /api/registrations/event/{eventId} — register for event (STUDENT)
 * PUT    /api/registrations/{id}/cancel     — cancel registration (STUDENT)
 * GET    /api/registrations/me              — my registrations (STUDENT)
 * GET    /api/registrations/{id}            — single registration (STUDENT)
 * GET    /api/registrations/event/{eventId} — all regs for event (ADMIN/ORGANIZER)
 */
@RestController
@RequestMapping("/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> registerForEvent(
            @PathVariable Long eventId,
            Authentication authentication) {

        String email = authentication.getName();
        RegistrationResponse response =
                registrationService.registerForEvent(eventId, email);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Successfully registered! QR ticket generated.", response
                ));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> cancelRegistration(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        RegistrationResponse response =
                registrationService.cancelRegistration(id, email);

        return ResponseEntity.ok(
                ApiResponse.success("Registration cancelled", response)
        );
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getMyRegistrations(
            Authentication authentication) {

        String email = authentication.getName();
        List<RegistrationResponse> registrations =
                registrationService.getMyRegistrations(email);

        return ResponseEntity.ok(
                ApiResponse.success("Registrations fetched", registrations)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> getRegistrationById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        RegistrationResponse response =
                registrationService.getRegistrationById(id, email);

        return ResponseEntity.ok(ApiResponse.success("Registration fetched", response));
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getRegistrationsByEvent(
            @PathVariable Long eventId) {

        List<RegistrationResponse> registrations =
                registrationService.getRegistrationsByEvent(eventId);

        return ResponseEntity.ok(
                ApiResponse.success("Registrations fetched", registrations)
        );
    }
}