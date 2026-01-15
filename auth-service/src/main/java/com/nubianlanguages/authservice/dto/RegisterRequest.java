package com.nubianlanguages.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class RegisterRequest {
    public RegisterRequest() {System.out.println("in registerrequest");}
//changed this
//    @Email
//    @NotBlank
//    private String name;
  //to this
//    @NotBlank
//    private String userName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

}
