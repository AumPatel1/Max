package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import org.example.matching.entity.User;
import org.example.matching.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DatabaseUserService {

    private final UserRepository userRepository;

    public User createUser(String username, String email, String passwordHash) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setBalance(BigDecimal.ZERO);
        user.setRole(User.UserRole.USER);
        
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User depositCash(String userId, BigDecimal amount) {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        BigDecimal newBalance = user.getBalance().add(amount);
        user.setBalance(newBalance);
        
        return userRepository.save(user);
    }

    public User findById(String userId) {
        return userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}
