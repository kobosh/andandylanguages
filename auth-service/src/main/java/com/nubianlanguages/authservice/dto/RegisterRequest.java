package com.nubianlanguages.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class RegisterRequest {
    public RegisterRequest() { /* TODO document why this constructor is empty */ }


    @Email
    @NotBlank
    private String email;
    @NotBlank

    private String name;
    @NotBlank

    private String role;
    @NotBlank
    @Size(min = 6)
    private String password;

}
