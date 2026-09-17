package com.eventmgmt.security;

import com.eventmgmt.entity.User;
import com.eventmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security calls this during authentication to load
 * user details from the database by email.
 *
 * Since our User entity implements UserDetails directly,
 * we simply return the User entity — no wrapper needed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by email (we use email as username).
     * Called by Spring Security during:
     * 1. Login (AuthenticationManager.authenticate())
     * 2. Every request (JwtAuthenticationFilter)
     *
     * @param email the user's email address
     * @return UserDetails (our User entity)
     * @throws UsernameNotFoundException if email not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException(
                            "User not found with email: " + email
                    );
                });

        log.debug("Loaded user: {} with role: {}", user.getEmail(), user.getRole());
        return user;
    }
}