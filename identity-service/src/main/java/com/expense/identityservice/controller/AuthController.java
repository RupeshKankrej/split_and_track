package com.expense.identityservice.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expense.identityservice.entity.User;
import com.expense.identityservice.entity.UserStatus;
import com.expense.identityservice.repository.UserRepository;
import com.expense.identityservice.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> postMethodName(@RequestBody User user) {
        User existingUser = userRepo.findByEmail(user.getEmail());

        if (existingUser != null) {
            if (existingUser.getStatus() == UserStatus.ACTIVE) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            if (existingUser.getStatus() == UserStatus.INVITED) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                existingUser.setName(user.getName()); // Update name if they want
                existingUser.setStatus(UserStatus.ACTIVE);
                userRepo.save(existingUser);
                return ResponseEntity.ok("Account claimed successfully! You can now see your groups.");
            }
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepo.save(user);
        return ResponseEntity.ok().body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = userRepo.findByEmail(loginRequest.getEmail());

        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail()));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
