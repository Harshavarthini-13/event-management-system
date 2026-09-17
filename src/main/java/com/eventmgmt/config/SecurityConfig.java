package com.eventmgmt.config;

import com.eventmgmt.security.CustomUserDetailsService;
import com.eventmgmt.security.JwtAuthenticationEntryPoint;
import com.eventmgmt.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Main Spring Security configuration.
 *
 * Key decisions:
 * - STATELESS session: no HTTP sessions, each request must carry JWT
 * - CSRF disabled: not needed for stateless REST APIs
 * - CORS enabled: allows React frontend on localhost:3000
 * - BCrypt: industry standard password hashing (strength 12)
 * - @EnableMethodSecurity: enables @PreAuthorize on controller methods
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Public endpoints — no JWT required.
     * Everything else requires authentication.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/register",
            "/auth/login",
            "/events",                  // Students can browse events without login
            "/events/{id}",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for JWT-based stateless APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Stateless session — Spring Security won't create HTTP sessions
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure endpoint authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/events").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/events/{id}").permitAll()

                        // WebSocket endpoint
                        .requestMatchers("/ws/**").permitAll()

                        // Static files (QR codes, certificates)
                        .requestMatchers("/qr-codes/**").permitAll()
                        .requestMatchers("/certificates/**").permitAll()

                        // Admin only endpoints
                        .requestMatchers(HttpMethod.POST,   "/events").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/events/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Organizer endpoints
                        .requestMatchers("/attendance/scan").hasRole("ORGANIZER")
                        .requestMatchers("/attendance/dashboard/**").hasAnyRole("ORGANIZER", "ADMIN")

                        // Student endpoints
                        .requestMatchers("/registrations/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/feedback/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/certificates/**").hasAnyRole("STUDENT", "ADMIN")

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )

                // Set custom authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before Spring's UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder with strength 12.
     * Used to hash passwords on registration and verify on login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * DaoAuthenticationProvider wires together:
     * - UserDetailsService (loads user from DB)
     * - PasswordEncoder (verifies BCrypt hash)
     *
     * Spring Security calls this during AuthenticationManager.authenticate()
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager is used in AuthService to verify
     * email + password during login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS configuration.
     * Allows React frontend (localhost:3000) to call our API.
     * In production, replace localhost:3000 with your actual domain.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed origins
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",   // React dev server
                "http://localhost:5173"    // Vite dev server
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allowed headers
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        // Allow credentials (needed for Authorization header)
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}