package com.eventmgmt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every HTTP request and:
 * 1. Extracts the JWT from the Authorization header
 * 2. Validates the token
 * 3. Loads the user from DB
 * 4. Sets the Authentication in SecurityContext
 *
 * Extends OncePerRequestFilter — guaranteed to run exactly once per request.
 *
 * Flow:
 * Request → JwtAuthenticationFilter → SecurityContext → Controller
 *
 * If token is missing or invalid, the filter simply does nothing and
 * Spring Security's AnonymousAuthenticationFilter handles the request
 * (resulting in 401 for protected endpoints).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Step 1: Extract token from "Authorization: Bearer <token>" header
            String jwt = extractJwtFromRequest(request);

            // Step 2: Validate token
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // Step 3: Get email from token
                String email = jwtTokenProvider.getEmailFromToken(jwt);

                // Step 4: Load UserDetails from DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Step 5: Create authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Attach request details (IP, session)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 6: Set authentication in SecurityContext
                // From this point, any @AuthenticationPrincipal or
                // SecurityContextHolder.getContext().getAuthentication() works
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception ex) {
            // Log but don't throw — let Spring Security handle the 401
            log.error("Could not set user authentication in security context: {}",
                    ex.getMessage());
        }

        // Always continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw JWT string from the Authorization header.
     * Header format: "Authorization: Bearer eyJhbGci..."
     *
     * @return raw token string or null if header is missing/malformed
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
}