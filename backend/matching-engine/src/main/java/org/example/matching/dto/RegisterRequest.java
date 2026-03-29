package org.example.matching.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/auth/register.
 *
 * <pre>
 * Constraints:
 *   username  →  3–30 chars, letters/digits/underscores only
 *   password  →  8+ chars minimum
 * Role is intentionally NOT accepted from the client — every user registers
 * as "USER". Admin privileges must be granted separately (DB or admin endpoint).
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Username may only contain letters, digits, and underscores"
    )
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
