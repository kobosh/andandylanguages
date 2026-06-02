package com.nubianlanguages.audioservices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestXX {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;


}
