package com.eventmgmt.dto.response;

import com.eventmgmt.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after successful login or registration.
 * Contains the JWT token and basic user info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Long userId;
    private String name;
    private String email;
    private Role role;
    private String token;
    private String tokenType;
    private Long expiresIn;     // milliseconds

    public static AuthResponse of(Long userId, String name,
                                  String email, Role role,
                                  String token, Long expiresIn) {
        return AuthResponse.builder()
                .userId(userId)
                .name(name)
                .email(email)
                .role(role)
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}