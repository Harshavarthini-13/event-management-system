package com.eventmgmt.service;

import com.eventmgmt.dto.request.LoginRequest;
import com.eventmgmt.dto.request.RegisterRequest;
import com.eventmgmt.dto.response.AuthResponse;
import com.eventmgmt.entity.User;
import com.eventmgmt.exception.ResourceNotFoundException;
import com.eventmgmt.repository.UserRepository;
import com.eventmgmt.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and login.
 *
 * Registration flow:
 * 1. Validate email is not already taken
 * 2. Hash password with BCrypt
 * 3. Save user to DB
 * 4. Generate JWT token
 * 5. Return AuthResponse
 *
 * Login flow:
 * 1. AuthenticationManager verifies email + password
 * 2. If valid, generate JWT token
 * 3. Return AuthResponse with token + user info
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtTokenProvider     jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user.
     *
     * @param request name, email, password, role
     * @return AuthResponse with JWT token
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email is already registered: " + request.getEmail()
            );
        }

        // Build and save the user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} with role: {}", user.getEmail(), user.getRole());

        // Generate JWT token for immediate login after registration
        String token = jwtTokenProvider.generateTokenFromEmail(user.getEmail());

        return AuthResponse.of(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token,
                jwtTokenProvider.getExpirationMs()
        );
    }

    /**
     * Authenticate user and return JWT token.
     *
     * @param request email + password
     * @return AuthResponse with JWT token
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        // Delegate to Spring Security AuthenticationManager
        // This calls CustomUserDetailsService.loadUserByUsername()
        // and BCrypt password verification internally
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Store authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Load user from DB for response data
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", request.getEmail()
                ));

        // Generate JWT
        String token = jwtTokenProvider.generateToken(authentication);
        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.of(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token,
                jwtTokenProvider.getExpirationMs()
        );
    }
}