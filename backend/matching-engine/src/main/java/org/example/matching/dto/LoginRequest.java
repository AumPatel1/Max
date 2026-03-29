package org.example.matching.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/auth/login.
 *
 * <pre>
 * Both fields are required. Spring Security's AuthenticationManager will
 * reject the request with BadCredentialsException if credentials are wrong —
 * that exception is caught in AuthController and translated to a 401.
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
