package com.eventmgmt.security;

import com.eventmgmt.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Called by Spring Security when an unauthenticated request
 * tries to access a protected endpoint.
 *
 * Without this, Spring Security returns its default HTML login page.
 * With this, we return a clean JSON 401 response that our React
 * frontend can handle properly.
 *
 * Example response:
 * HTTP 401
 * {
 *   "success": false,
 *   "message": "Unauthorized: Full authentication is required to access this resource",
 *   "timestamp": "2024-01-01T10:00:00"
 * }
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {

        log.error("Unauthorized request to [{}]: {}",
                request.getRequestURI(), authException.getMessage());

        // Build JSON error response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Void> errorResponse = ApiResponse.error(
                "Unauthorized: " + authException.getMessage()
        );

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}