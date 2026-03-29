package org.example.matching.security;

import lombok.RequiredArgsConstructor;
import org.example.matching.Repository.UserRepository;
import org.example.matching.entity.UserEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // "ROLE_" prefix is required by Spring Security's hasRole() checks.
        // The DB stores "USER" or "ADMIN"; we prefix it here so Spring sees "ROLE_USER" / "ROLE_ADMIN".
        // hasAuthority("USER") would also work without the prefix, but ROLE_ prefix is the convention.
        return new User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        /**
         *  Spring Security doesn't know about your UserEntity — it only understands its own UserDetails interface.
         * Your World                    Spring Security's World
         * ----------                    -----------------------
         * UserEntity          →         UserDetails
         * (your JPA entity)             (Spring's interface)
         *
         * user.getUsername()  →         getUserUsername()
         * user.getPassword()  →         getPassword()
         * user.getRole()      →         getAuthorities()
         * Spring Security internally does things like:
         * java// Spring does this automatically during authentication
         * userDetails.getPassword()      // check password
         * userDetails.getAuthorities()   // check roles
         * userDetails.isAccountExpired() // check account status
         * ```
         *
         * It **cannot call** `userEntity.getPassword()` directly — it doesn't know what `UserEntity` is.
         *
         * So the flow is:
         * ```
         * Login request
         *      ↓
         * Spring calls loadUserByUsername()
         *      ↓
         * You fetch UserEntity from DB        ← your world
         *      ↓
         * You wrap it in new User(...)        ← translation happens here
         *      ↓
         * Spring gets UserDetails back        ← Spring's world
         *      ↓
         * Spring validates password, roles etc automatically
         */
    }
}