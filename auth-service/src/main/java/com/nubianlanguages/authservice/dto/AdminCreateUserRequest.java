package com.nubianlanguages.authservice.dto;

import com.nubianlanguages.authservice.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class AdminCreateUserRequest {
    private String fullName;
    private String email;
    private String temporaryPassword;
    private Role role;

    // getters and setters
}