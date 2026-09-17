package com.eventmgmt.controller;

import com.eventmgmt.dto.response.ApiResponse;
import com.eventmgmt.dto.response.UserResponse;
import com.eventmgmt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            com.eventmgmt.entity.User currentUser) {
        return ResponseEntity.ok(
                ApiResponse.success("Profile fetched",
                        userService.getUserById(currentUser.getId()))
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success("Users fetched", userService.getAllUsers())
        );
    }

    @GetMapping("/organizers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllOrganizers() {
        return ResponseEntity.ok(
                ApiResponse.success("Organizers fetched", userService.getAllOrganizers())
        );
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("User deactivated", null)
        );
    }
}