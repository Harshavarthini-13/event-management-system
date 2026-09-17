package com.eventmgmt.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Responsible for:
 * 1. Generating JWT tokens after successful login
 * 2. Extracting username (email) from a token
 * 3. Validating tokens (signature, expiry, format)
 *
 * Uses JJWT 0.12.x API (Jwts.builder(), verifyWith(), etc.)
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Build the signing key from the base64-encoded secret string.
     * HMAC-SHA256 requires at least 256-bit key.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder()
                        .encodeToString(jwtSecret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT token for an authenticated user.
     * Called in AuthService after password verification succeeds.
     *
     * @param authentication Spring Security Authentication object
     * @return signed JWT string
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildToken(userDetails.getUsername());
    }

    /**
     * Generate token directly from email string.
     * Used in AuthService register flow.
     */
    public String generateTokenFromEmail(String email) {
        return buildToken(email);
    }

    private String buildToken(String email) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract the email (subject) from a valid JWT token.
     */
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Returns the configured expiration in milliseconds.
     * Included in AuthResponse so the client knows when to refresh.
     */
    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * Validate a JWT token.
     * Returns true if the token is well-formed, signed correctly, and not expired.
     * Logs the specific reason for rejection to aid debugging.
     *
     * @param token raw JWT string from Authorization header
     * @return true if valid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;

        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token format: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT token has expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}