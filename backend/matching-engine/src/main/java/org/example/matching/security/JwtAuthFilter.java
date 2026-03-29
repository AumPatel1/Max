package org.example.matching.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter — runs once per HTTP request (OncePerRequestFilter guarantee).
 *
 * Every request
 *       ↓
 * Read "Authorization: Bearer <token>" header
 *       ↓
 * Strip "Bearer " → get raw token
 *       ↓
 * extractUsername(token)         ← JwtUtil
 *       ↓
 * loadUserByUsername(username)   ← UserDetailsServiceImpl hits DB
 *       ↓
 * validateToken()                ← check not expired, signature valid
 *       ↓
 * setAuthentication()            ← tell Spring Security "this user is authenticated"
 *       ↓
 * continue to your controller
 *
 * If the token is malformed, expired, or tampered with, the filter responds with
 * 401 Unauthorized immediately — the request never reaches the controller.
 */
// NO @Component here — if marked @Component, Spring Boot auto-registers this as a servlet filter
// (outside the security chain). It then runs before SecurityContextHolderFilter, sets auth, but
// SecurityContextHolderFilter replaces the context with a fresh empty one. OncePerRequestFilter
// then skips the security-chain execution (already ran flag is set). AuthorizationFilter sees
// empty context → 403. Fix: no @Component, bean is created manually inside SecurityConfig so it
// only ever runs inside the Spring Security filter chain.
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    // final → injected by @RequiredArgsConstructor via constructor.
    // Without final, @RequiredArgsConstructor ignores the field and it stays null → NPE on every request.
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // ── No token present — pass through (SecurityConfig rules decide if auth is required) ──
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token    = authHeader.substring(7); // strip the "Bearer " prefix (7 chars)
        String username = null;

        try {
            username = jwtUtil.extractUsername(token);
        } catch (JwtException | IllegalArgumentException e) {
            // Token is malformed, tampered, or has an invalid signature
            log.warn("Rejected malformed JWT for {}: {}", request.getRequestURI(), e.getMessage());
            sendUnauthorized(response, "Invalid or malformed token");
            return;
        }

        /*
         * Only set authentication if:
         *   1. Username was successfully extracted from the token
         *   2. SecurityContextHolder doesn't already hold an authenticated principal
         *      (prevents overwriting auth set by an earlier filter in the chain)
         *
         * if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
         *                                              // ↑ only set if not already authenticated
         *                                              // prevents overwriting existing authentication
         * }
         */
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(username, token)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                         // credentials null — JWT is the credential
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                /*
                 * WebAuthenticationDetailsSource attaches request metadata (IP address, session ID)
                 * to the authentication object — useful for audit logging and future IP-based checks.
                 */
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Authenticated '{}' via JWT on {}", username, request.getRequestURI());
            } else {
                // Token is well-formed but expired or username does not match
                sendUnauthorized(response, "Token expired or invalid");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Writes a 401 JSON error response without letting the request propagate further.
     * Keeps the error contract consistent — clients always get JSON, not an HTML error page.
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\",\"status\":401}");
    }
}
