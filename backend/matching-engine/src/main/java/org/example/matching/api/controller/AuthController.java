package org.example.matching.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.matching.Wallets.WalletService;
import org.example.matching.dto.AuthResponse;
import org.example.matching.dto.LoginRequest;
import org.example.matching.dto.RegisterRequest;
import org.example.matching.entity.UserEntity;
import org.example.matching.Repository.UserRepository;
import org.example.matching.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller — handles user registration and login.
 *
 * REGISTER
 * ──────────────────────────────────────────
 * findByUsername()          →  username already exists? → 409 Conflict
 * passwordEncoder.encode()  →  "john123" → "$2a$10$xK9zQ..."  (BCrypt hash)
 * userRepository.save()     →  store in DB
 * walletService.createWallet() → create an empty wallet for the new user
 * generateToken()           →  create signed JWT
 * return AuthResponse       →  token + tokenType + username + role + expiresIn
 *
 * LOGIN
 * ──────────────────────────────────────────
 * authenticationManager.authenticate()
 *       ↓
 *       internally does:
 *       loadUserByUsername()  →  fetch UserEntity from DB
 *       BCrypt.matches()      →  compare raw vs stored hash
 *       wrong → throws BadCredentialsException → caught → 401
 *       correct → Authentication object returned
 *       ↓
 * generateToken()  →  create signed JWT
 * return AuthResponse
 *
 * How everything links together:
 *
 * POST /api/auth/register
 *       ↓
 * SecurityConfig → permitAll() → no token needed
 *       ↓
 * AuthController.register()
 *       ↓
 * BCrypt hashes password → userRepository.save() → walletService.createWallet()
 *       ↓
 * jwtUtil.generateToken() → returns token to client
 *
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *
 * POST /api/auth/login
 *       ↓
 * SecurityConfig → permitAll() → no token needed
 *       ↓
 * AuthController.login()
 *       ↓
 * authenticationManager → UserDetailsServiceImpl → BCrypt compare
 *       ↓
 * jwtUtil.generateToken() → returns token to client
 *
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *
 * GET /api/orders  (protected endpoint example)
 *       ↓
 * JwtAuthFilter → reads token → validateToken()
 *       ↓
 * SecurityContextHolder set → request goes through
 *       ↓
 * Controller reached
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final WalletService walletService;

    // ─── REGISTER ───────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        // Step 1 — check username is not already taken
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\":\"Username already exists\"}");
        }

        // Step 2 — hash password and persist user
        // Role is always "USER" — clients cannot self-assign ADMIN via the API
        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hash
                .role("USER")
                .build();

        userRepository.save(user);

        // Step 3 — auto-create an empty wallet for the new user
        // Every user starts with 0 cash and 0 shares; they deposit via /api/wallets/depositCash
        walletService.createWallet(user.getUsername());
        log.info("Registered new user '{}' and created wallet", user.getUsername());

        // Step 4 — generate JWT so the client is immediately authenticated after registering
        String token = jwtUtil.generateToken(user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponse.builder()
                        .token(token)
                        .tokenType("Bearer")
                        .username(user.getUsername())
                        .role(user.getRole())
                        .expiresIn(jwtUtil.getExpirationMs())
                        .build()
        );
    }

    // ─── LOGIN ──────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        try {
            // Step 1 — AuthenticationManager verifies credentials
            // Internally: loadUserByUsername(username) → BCrypt.matches(raw, stored hash)
            // Throws BadCredentialsException if username not found or password wrong
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Step 2 — credentials valid; authentication.getName() returns the username (subject)
            String username = authentication.getName();

            // Step 3 — fetch user for role info (exists because authentication succeeded)
            UserEntity user = userRepository.findByUsername(username).orElseThrow();

            // Step 4 — generate signed JWT
            String token = jwtUtil.generateToken(username);
            log.info("User '{}' logged in", username);

            return ResponseEntity.ok(
                    AuthResponse.builder()
                            .token(token)
                            .tokenType("Bearer")
                            .username(username)
                            .role(user.getRole())
                            .expiresIn(jwtUtil.getExpirationMs())
                            .build()
            );

        } catch (BadCredentialsException e) {
            // Wrong username or password — return 401, never reveal which one was wrong
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Invalid username or password\"}");
        }
    }
}
