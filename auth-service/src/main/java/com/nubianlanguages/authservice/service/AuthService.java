package com.nubianlanguages.authservice.service;

import com.nubianlanguages.authservice.dto.AdminCreateUserRequest;
import com.nubianlanguages.authservice.dto.ChangePasswordRequest;
import com.nubianlanguages.authservice.dto.LoginRequest;
import com.nubianlanguages.authservice.model.AppUser;
import com.nubianlanguages.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    public AuthService(JavaMailSender mailSender, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }
   public  Optional<AppUser> login(String email) {


       return userRepository.findByEmail(email);




   }
   public  AppUser register(String email,String pwd,String fullName)
   {
       System.out.println( email+pwd+fullName);
       if (email == null || pwd== null || fullName==null) {
           System.out.println("Email already exists");
           return  null;// "Email and password  and full name are required";
       }

       if (userRepository.findByEmail(email).isPresent()) {
           System.out.println("Email already exists");
           return  null;// "Email already exists";
       }
       AppUser user = new AppUser();
       user.setEmail(email);
       user.setPassword(pwd);
       user.setFullname(fullName);
       try {
           userRepository.save(user);
       } catch (Exception e) {
           System.out.println("EXCEPTION "+ e.getMessage());
   }

System.out.println(" User "+user.toString());

       return user;
   }
    public void sendResetEmail(String email, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Andandy Languages Password Reset");
        message.setText(
                "Click the link below to reset your password:\n\n"
                        + resetLink
        );

        mailSender.send(message);
    }

    public boolean forgotPassword(String email) {

        Optional<AppUser> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return false;
        }

        AppUser user = optionalUser.get();

        String token = UUID.randomUUID().toString();

        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(30));

        userRepository.save(user);

        String resetLink =
                "http://localhost:4200/forgot-password?token=" + token;

        System.out.println("RESET LINK: " + resetLink);

        sendResetEmail(user.getEmail(), resetLink);

        return true;
    }

    public boolean changePassword(String email, String oldPwd, String newPwd) {

        Optional<AppUser> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return false;
        }

        AppUser user = optionalUser.get();

        if (!user.getPassword().equals(oldPwd)) {
            return false;
        }

        user.setPassword(newPwd); // plain password for now
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        user.setMustChangePassword(false);

        userRepository.save(user);

        return true;
    }
    public boolean adminCreateUser(String email,String fullName, String role) {

        AppUser user = new AppUser();
        user.setFullname(fullName);
        user.setEmail(email);
        user.setRole(String.valueOf(role)); // CONTRIBUTOR or LEARNER
       // user.setPassword(passwordEncoder.encode(req.getTemporaryPassword()));
        user.setMustChangePassword(true);

        userRepository.save(user);

        return  true;//ResponseEntity.ok("User created successfully");
    }
}
