package com.nubianlanguages.authservice.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ForgotPasswordRequest {
    private String email;
    private String password;
}