package com.nubianlanguages.authservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String password;   // BCrypt hash

    @NotBlank
    @Column(nullable = false)
    private String role = "ROLE_USER";

    public AppUser() {}

    public AppUser(String email, String password, String role) {
        //this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
