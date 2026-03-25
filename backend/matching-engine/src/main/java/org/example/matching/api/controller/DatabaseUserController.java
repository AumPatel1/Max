package org.example.matching.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.matching.api.dto.DepositRequest;
import org.example.matching.api.service.DatabaseUserService;
import org.example.matching.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/db/users")
@RequiredArgsConstructor
public class DatabaseUserController {

    private final DatabaseUserService userService;

    @PostMapping("/deposit")
    public ResponseEntity<String> depositCash(@RequestBody DepositRequest request) {
        try {
            User user = userService.depositCash(request.getUserId(), BigDecimal.valueOf(request.getAmount()));
            return ResponseEntity.ok("Deposit successful. New balance: " + user.getBalance());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Deposit failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> registration) {
        try {
            User user = userService.createUser(
                registration.get("username"),
                registration.get("email"), 
                "hashed_password" // In real app, hash this!
            );
            return ResponseEntity.ok("User created with ID: " + user.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        try {
            User user = userService.findById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
