package com.eventmgmt.service;

import com.eventmgmt.dto.response.UserResponse;
import com.eventmgmt.entity.User;
import com.eventmgmt.enums.Role;
import com.eventmgmt.exception.ResourceNotFoundException;
import com.eventmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles user profile retrieval and admin user management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get user profile by ID.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserResponse.fromEntity(user);
    }

    /**
     * Get all users (Admin only).
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all organizers — used by Admin when creating/assigning events.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllOrganizers() {
        return userRepository.findAllActiveOrganizers()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all users by role.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Deactivate a user (soft delete).
     * Admin only.
     */
    @Transactional
    public UserResponse deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsActive(false);
        user = userRepository.save(user);
        log.info("User deactivated: {}", user.getEmail());
        return UserResponse.fromEntity(user);
    }

    /**
     * Activate a user.
     * Admin only.
     */
    @Transactional
    public UserResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsActive(true);
        user = userRepository.save(user);
        log.info("User activated: {}", user.getEmail());
        return UserResponse.fromEntity(user);
    }

    /**
     * Get currently logged-in user entity by email.
     * Used internally by other services.
     */
    @Transactional(readOnly = true)
    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}