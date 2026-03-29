package org.example.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body returned by both /api/auth/register and /api/auth/login.
 *
 * <pre>
 * Fields:
 *   token      →  signed JWT string (header.payload.signature)
 *   tokenType  →  always "Bearer" — tells the client how to send it:
 *                   Authorization: Bearer &lt;token&gt;
 *   username   →  the authenticated user's username
 *   role       →  the user's role (e.g. "USER", "ADMIN")
 *   expiresIn  →  token lifetime in milliseconds (for client-side expiry tracking)
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    /** Always "Bearer" — the scheme the client must use in the Authorization header. */
    private String tokenType;

    private String username;
    private String role;

    /** Milliseconds until the token expires (same as jwt.expiration in application.properties). */
    private long expiresIn;
}
