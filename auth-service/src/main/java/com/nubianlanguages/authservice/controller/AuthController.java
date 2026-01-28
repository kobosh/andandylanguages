package com.nubianlanguages.authservice.controller;

import com.nubianlanguages.authservice.dto.LoginRequest;
import com.nubianlanguages.authservice.dto.RegisterRequest;
import com.nubianlanguages.authservice.model.AppUser;
import com.nubianlanguages.authservice.repository.UserRepository;
import com.nubianlanguages.authservice.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final  PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public AuthController(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        AppUser user = userRepository.findByEmail(request.getEmail())
                .filter(u -> u.getPassword().equals(request.getPassword())) // example
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // 🔑 Authentication already succeeded
        String token = jwtService.generateToken(
                user.getId().toString(),
                expirationMs,
                user.getFullname()
        );

        return ResponseEntity.ok(
                Map.of(
                        "accessToken", token,
                        "expiresIn", expirationMs / 1000
                )
        );
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        // 1️⃣ Validate input (minimal, but necessary)
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }

        // 2️⃣ Prevent duplicate users
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body("Email already exists");
        }

        // 3️⃣ Create user
        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        // ⚠️ Plain text for now (OK for dev, NOT prod)
        user.setPassword(request.getPassword());
       // user.setPassword(passwordEncoder.encode(request.getPassword()));
        // OPTIONAL: only set name if it exists
        if (request.getName() != null) {
            user.setFullname(request.getName());
        }

        userRepository.save(user);

        System.out.println("✅ Saved to H2 MEM: " + user.getEmail());
// 4️⃣ Issue JWT immediately after register
        String token = jwtService.generateToken(
                user.getId().toString(),
                expirationMs, user.getFullname()

        );

// 5️⃣ Return token + expiration
        return ResponseEntity.status(201).body(
                Map.of(
                        "accessToken", token,
                        "expiresIn", expirationMs / 1000,          // seconds
                        "expiresAt",
                        user.getFullname()// epoch ms

                )
        );


    }

}

