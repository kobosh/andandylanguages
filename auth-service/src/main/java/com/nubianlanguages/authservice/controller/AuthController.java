/*package com.nubianlanguages.authservice.controller;

import com.nubianlanguages.authservice.dto.*;
import com.nubianlanguages.authservice.model.AppUser;
import com.nubianlanguages.authservice.repository.UserRepository;
import com.nubianlanguages.authservice.security.JwtService;
import com.nubianlanguages.authservice.service.AuthService;
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
    private final AuthService authService;
    private final  PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public AuthController(JwtService jwtService, AuthService authService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.authService = authService;

        this.passwordEncoder = passwordEncoder;
    }


   @PostMapping("/login")
   public ResponseEntity<?> login(@RequestBody LoginRequest request) {


       Optional<AppUser> optionalUser = this.authService.login(request.getEmail());

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




       // ✅ SET ROLE (IMPORTANT)
       AppUser user=this.authService.register(request.getEmail(),request.getPassword(),
               request.getName());
       System.out.println("CONTROLLER "+user.toString());
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
    public ResponseEntity<?> adminCreateUser(
            @RequestBody AdminCreateUserRequest req) {

        boolean created = authService.adminCreateUser(
                req.getEmail(),
                req.getFullName(),
                String.valueOf(req.getRole())
        );

        return created
                ? ResponseEntity.ok("User created successfully")
                : ResponseEntity.badRequest().body("User already exists");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        this.authService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(" if email exists, reset link was sent");
    }
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request) {

        boolean changed = authService.changePassword(
                request.getEmail(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        if (!changed) {
            return ResponseEntity.badRequest()
                    .body("Invalid email or old password");
        }

        return ResponseEntity.ok("Password changed successfully");
    }

}*/
package com.nubianlanguages.authservice.controller;

import com.nubianlanguages.authservice.dto.*;
import com.nubianlanguages.authservice.model.AppUser;
import com.nubianlanguages.authservice.repository.UserRepository;
import com.nubianlanguages.authservice.security.JwtService;
import com.nubianlanguages.authservice.service.AuthService;
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
    private  final AuthService authService;
    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public AuthController(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder, AuthService authService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
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
                 System.out.println("USER ROLE "+user.getRole());
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
         if(request.getRole().equalsIgnoreCase("contrib"))
         { user.setMustChangePassword(true);}
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

        System.out.println("EMAIL " + request.getEmail());

       /* Optional<AppUser> optionalUser =
                userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.ok(
                    "If email exists, reset link was sent"
            );
        }

        AppUser user = optionalUser.get();



        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiresAt(
                LocalDateTime.now().plusMinutes(30)
        );

        userRepository.save(user);*/
               String token = UUID.randomUUID().toString();
        String resetLink =
                "http://localhost:4200/reset-password?token=" + token;

        System.out.println("RESET LINK: " + resetLink);

        authService.sendResetEmail(request.getEmail(), resetLink);

        return ResponseEntity.ok("email exists, reset link was sent");
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request) {
        System.out.println("REQUEST RESET "+request);
        Optional<AppUser> optionalUser =
                userRepository.findByEmail(
                        request.getEmail());

        if (optionalUser.isEmpty()) {
            System.out.println("OPTIONAL USER EMPTY");
            return ResponseEntity.badRequest()
                    .body("Invalid token");
        }

        AppUser user = optionalUser.get();

        user.setPassword(request.getNewPassword());

        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        user.setMustChangePassword(false);

        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully");
    }
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
       //System.out.println("old pass"+request.getOldPassword());
        Optional<AppUser> optionalUser =
                userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            System.out.println("If old password exists, reset link was sent");
            return ResponseEntity
                    .badRequest()
                    .body("Invalid reset token");
        }

        AppUser user = optionalUser.get();



        user.setPassword(request.getNewPassword()); // plain password for now
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        user.setMustChangePassword(false);

        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully");
    }

}


