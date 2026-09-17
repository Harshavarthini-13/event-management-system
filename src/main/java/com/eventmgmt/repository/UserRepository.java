package com.eventmgmt.repository;

import com.eventmgmt.entity.User;
import com.eventmgmt.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 * Spring Data JPA auto-implements all methods at runtime.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used by Spring Security to load user during login
    Optional<User> findByEmail(String email);

    // Check if email already exists before registration
    boolean existsByEmail(String email);

    // Admin: get all users by role
    List<User> findByRole(Role role);

    // Admin: get all active users
    List<User> findByIsActiveTrue();

    // Admin: get all organizers (for assigning to events)
    @Query("SELECT u FROM User u WHERE u.role = 'ORGANIZER' AND u.isActive = true")
    List<User> findAllActiveOrganizers();

    // Count users by role (for dashboard stats)
    long countByRole(Role role);
}