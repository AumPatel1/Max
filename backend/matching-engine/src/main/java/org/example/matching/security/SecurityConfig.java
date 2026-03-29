package org.example.matching.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * Security model:
 *   PUBLIC        →  /api/auth/**    (register, login — no token needed)
 *                    /api/market/**  (order book snapshots, tickers — read-only public data)
 *                    /ws/**          (WebSocket market data feed — public stream)
 *   AUTHENTICATED →  everything else (/api/orders/**, /api/wallets/**, /api/events/**)
 *
 * Session strategy: STATELESS — every request must carry a JWT in the Authorization header.
 *                   No HttpSession is ever created or consulted on the server side.
 *
 * WHY JwtAuthFilter is NOT @Component (important):
 *   If @Component is placed on JwtAuthFilter, Spring Boot auto-registers it as a servlet-level
 *   filter (runs BEFORE Spring Security's FilterChainProxy). The flow then breaks:
 *     1. JwtAuthFilter (servlet level) sets authentication in SecurityContextHolder
 *     2. SecurityContextHolderFilter (inside security chain) replaces context with fresh empty one
 *     3. JwtAuthFilter (inside security chain) → OncePerRequestFilter detects "already ran" → SKIPS
 *     4. AuthorizationFilter sees empty context → 403 Forbidden
 *
 *   Fix: JwtAuthFilter has no @Component. SecurityConfig constructs it manually with 'new' and
 *   adds it only to the security filter chain. Spring Boot never sees it as a bean to auto-register.
 *
 * Filter order (before Spring's default login filter):
 *   Request → JwtAuthFilter → UsernamePasswordAuthenticationFilter → Controller
 *
 * JwtAuthFilter sets SecurityContextHolder if the token is valid.
 * UsernamePasswordAuthenticationFilter sees the context already filled and skips its job.
 */
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize / @Secured annotations on controller methods
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // Injected to construct JwtAuthFilter manually inside securityFilterChain.
    // JwtUtil and UserDetailsServiceImpl are Spring beans; JwtAuthFilter is NOT (see class comment above).
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Construct JwtAuthFilter here — NOT as a Spring bean, only as an instance used inside
        // this filter chain. This prevents Spring Boot from auto-registering it as a servlet filter.
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtil, userDetailsService);

        http
            .csrf(csrf -> csrf.disable())
            /*
             * CSRF disabled because:
             *   - We use stateless JWT — no session cookies are issued
             *   - CSRF attacks rely on the browser sending cookies automatically
             *   - Without session cookies there is nothing to forge
             */
            .authorizeHttpRequests(auth -> auth
                // ── Public endpoints (no token required) ─────────────────
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/market/**").permitAll()    // read-only market data
                .requestMatchers("/ws/**").permitAll()            // WebSocket feed
                // ── Everything else requires a valid JWT ──────────────────
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                /*
                 * STATELESS: Spring will never create or look up an HttpSession.
                 * Authentication state lives only in the JWT, not on the server.
                 */
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            /*
             * addFilterBefore:
             *   JwtAuthFilter executes first, validates the token, and sets
             *   the Authentication object in SecurityContextHolder.
             *   UsernamePasswordAuthenticationFilter then sees the context is
             *   already populated and passes the request through untouched.
             *
             * JwtAuthFilter fills SecurityContextHolder first
             *       ↓
             * UsernamePasswordAuthenticationFilter sees it filled
             *       ↓
             * says "someone already handled this, moving on"
             *       ↓
             * request reaches your controller safely
             */

        return http.build();
    }

    /**
     * BCryptPasswordEncoder — used to hash passwords on registration
     * and to verify them on login.
     *
     * Strength defaults to 10 rounds (2^10 iterations of bcrypt).
     * Higher = slower hash = harder to brute-force (~100 ms per hash at strength 10).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /***
     *
     * AuthenticationManager
     * java@Bean
     * public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
     *         throws Exception {
     *     return config.getAuthenticationManager();
     * }
     * ```
     * This is Spring's built in component that handles login:
     * ```
     * You give it  →  username + raw password
     * It does      →  loads user from DB via UserDetailsServiceImpl
     *              →  compares passwords via BCryptPasswordEncoder
     *              →  returns authenticated user OR throws exception
     * You'll use it in your login controller like:
     * javaauthenticationManager.authenticate(
     *     new UsernamePasswordAuthenticationToken(username, password)
     * );
     * ```
     * You expose it as a `@Bean` so you can inject it into your `AuthController` later.
     *
     * ---
     *
     * **`UsernamePasswordAuthenticationFilter` seeing SecurityContextHolder already set:**
     *
     * The filter chain runs like this:
     * ```
     * Request → JwtAuthFilter runs first
     *                ↓
     *         reads token → valid
     *                ↓
     *         SecurityContextHolder.getContext().setAuthentication(authToken)
     *         // SecurityContextHolder is now FILLED
     *                ↓
     *         filterChain.doFilter() → moves to next filter
     *                ↓
     * UsernamePasswordAuthenticationFilter runs next
     *                ↓
     *         internally checks:
     *         if (SecurityContextHolder.getContext().getAuthentication() != null) {
     *             // already authenticated, skip my job
     *         }
     *                ↓
     *         passes request through to controller
     * That's exactly why you also check this in JwtAuthFilter:
     * java// you check this before setting authentication
     * if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
     *                                              // ↑ only set if not already authenticated
     *                                              // prevents overwriting existing authentication
     * }
     * ```
     *
     * Simply:
     * ```
     * JwtAuthFilter fills SecurityContextHolder first
     *       ↓
     * UsernamePasswordAuthenticationFilter sees it filled
     *       ↓
     * says "someone already handled this, moving on"
     *       ↓
     * request reaches your controller safely
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
