package org.example.matching.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT utility — responsible for:
 *   - Generating signed tokens (HS256)
 *   - Parsing and validating tokens
 *   - Extracting the subject (username) from a token
 *
 * header.payload.signature
 *   ↓       ↓        ↓
 * algo    claims   verification
 *
 *The key is not inside the token at all:
 * header.payload.signature
 *=the important distinction:
 * SECRET KEY  →  never travels in the token
 *                stays on  server only
 *                used to CREATE and VERIFY the signature
 * The three parts:
 * header     →  algorithm info       (inside token)
 * payload    →  claims/your data     (inside token)
 * signature  →  proof of integrity   (inside token)
 * Think of it like a sealed letter:
 * header    =  envelope front    "signed using method X"
 * payload   =  the letter        "user is john, expires tomorrow"
 * signature =  wax seal          "proves nobody tampered"
 *
 * secret key = your personal stamp ring
 *            = used to MAKE the wax seal
 *            = never put inside the envelope
 *            = stays in your pocket always
 */
@Component   // registers as a Spring bean so @Value fields are injected by Spring
public class JwtUtil {

    // generate a token from username
    // extract it , add expiry and add secret key

    /**
     * Base64-encoded HMAC-SHA256 secret — must decode to at least 256 bits (32 bytes).
     * Set in application.properties: jwt.secret=<base64-string>
     *
     * IMPORTANT: Must NOT be final — Spring injects @Value fields via reflection after
     * construction. A final field cannot be set by reflection → IllegalAccessException.
     * @RequiredArgsConstructor (Lombok) is also NOT used here for the same reason:
     * it generates a constructor for final fields, not for @Value-injected ones.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token lifetime in milliseconds.
     * Set in application.properties: jwt.expiration=86400000  (= 24 hours)
     */
    @Value("${jwt.expiration}")
    private long expirationMs;

    // one is key and another is lock ( that is signing algorithm)

    /**
     * Decodes the Base64 secret and builds an HMAC-SHA signing key.
     * Keys.hmacShaKeyFor() enforces minimum 256-bit length for HS256 — fails fast
     * at startup if the configured secret is too short rather than running insecurely.
     */
    public SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Creates a signed JWT for the given username.
     *
     * Claims embedded:
     *   sub  → username  (standard JWT "subject")
     *   iat  → issued-at time
     *   exp  → expiry time (iat + expirationMs)
     *
     * @param username the authenticated user's username (becomes the token subject)
     * @return compact JWT string: header.payload.signature
     */
    public String generateToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the username (subject) from a JWT.
     *
     * @param token raw JWT string
     * @return the username stored in the token's subject claim
     * @throws JwtException if the token is malformed, expired, or tampered with
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Returns the configured token lifetime in milliseconds.
     * Used to populate the expiresIn field in AuthResponse.
     */
    public long getExpirationMs() {
        return expirationMs;
    }

    /**
     * Validates that:
     *   1. The token's subject matches expectedUsername
     *   2. The token has not yet expired
     *   3. The HMAC signature is intact (no tampering)
     *
     * @param username the username from UserDetails (loaded from DB)
     * @param token    raw JWT string
     * @return true if the token is valid for this user, false otherwise
     */
    public boolean validateToken(String username, String token) {
        try {
            String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            // Malformed, tampered, or expired token — treat as invalid, not an exception
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Parses the token and returns all claims.
     *
     * Flow:
     *   raw token string
     *         ↓
     *   Jwts.parser().verifyWith(signingKey)  — re-computes HMAC for comparison
     *         ↓
     *   if signature matches → return claims payload
     *   if tampered / expired → throw JwtException
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
