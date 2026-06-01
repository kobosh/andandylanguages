package com.nubianlanguages.authservice.controller;

import com.nubianlanguages.authservice.dto.*;
import com.nubianlanguages.authservice.model.AppUser;
import com.nubianlanguages.authservice.repository.UserRepository;
import com.nubianlanguages.authservice.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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


       Optional<AppUser> optionalUser = userRepository.findByEmail(request.getEmail());

       if (optionalUser.isEmpty()) {
           return ResponseEntity
                   .status(HttpStatus.UNAUTHORIZED)
                   .body(Map.of("message", "Invalid email or password"));
       }

       AppUser user = optionalUser.get();

       if (! request.getPassword().equals( user.getPassword()) ){
           return ResponseEntity
                   .status(HttpStatus.UNAUTHORIZED)
                   .body(Map.of("message", "Invalid email or password"));
       }

       String token = jwtService.generateToken(
               user.getId().toString(),
               expirationMs,
               user.getFullname()
       );

       return ResponseEntity.ok(
               Map.of(
                       "accessToken", token,
                       "expiresIn", expirationMs / 1000,
                       "role", user.getRole(),
                       "mustChangePassword", user.isMustChangePassword(),
                       "email",request.getEmail()
               )
       );
   }


   @PostMapping("/register")
   public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

       if (request.getEmail() == null || request.getPassword() == null) {
           return ResponseEntity.badRequest().body("Email and password are required");
       }

       if (userRepository.findByEmail(request.getEmail()).isPresent()) {
           return ResponseEntity.status(409).body("Email already exists");
       }

       AppUser user = new AppUser();
       user.setEmail(request.getEmail());
       user.setPassword(request.getPassword());

       if (request.getName() != null) {
           user.setFullname(request.getName());
       }

       // ✅ SET ROLE (IMPORTANT)
       if (request.getRole() != null) {
           user.setRole(request.getRole());
       } else {
           user.setRole("learner"); // default
       }

       userRepository.save(user);

       String token = jwtService.generateToken(
               user.getId().toString(),
               expirationMs,
               user.getFullname()
       );
       String role= user.getRole();


       return ResponseEntity.status(201).body(
               Map.of(
                       "accessToken", token,
                       "expiresIn", expirationMs / 1000,
                       "role", role  // optional
               )
       );
   }
    @PostMapping("/admin/create-user")
    public ResponseEntity<?> adminCreateUser(@RequestBody AdminCreateUserRequest req) {

        AppUser user = new AppUser();
        user.setFullname(req.getFullName());
        user.setEmail(req.getEmail());
        user.setRole(String.valueOf(req.getRole())); // CONTRIBUTOR or LEARNER
        user.setPassword(passwordEncoder.encode(req.getTemporaryPassword()));
        user.setMustChangePassword(true);

        userRepository.save(user);

        return ResponseEntity.ok("User created successfully");
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
System.out.println("EMAIL "+request.getEmail());
        Optional<AppUser> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.ok("If email does not exist, reset link was not sent");
        }

        AppUser user = optionalUser.get();

        String token = UUID.randomUUID().toString();

        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
        System.out.println("New Password "+request.getPassword());
        if(request.getPassword()!=null)
        { user.setPassword(request.getPassword());
           userRepository.save(user);}

        System.out.println(
                "RESET LINK: http://localhost:4200/forgot-password?token=" + token
        );

        return ResponseEntity.ok(" email exists, reset link was sent");
    }
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        System.out.println("old pass"+request.getOldPassword());
        Optional<AppUser> optionalUser =
                userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            System.out.println("If old password exists, reset link was sent");
            return ResponseEntity
                    .badRequest()
                    .body("Invalid reset token");
        }

        AppUser user = optionalUser.get();

        /*if (user.getPasswordResetTokenExpiresAt() == null ||
                user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {

            return ResponseEntity
                    .badRequest()
                    .body("Reset token expired");
        }*/

        user.setPassword(request.getNewPassword()); // plain password for now
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        user.setMustChangePassword(false);

        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully");
    }

}

