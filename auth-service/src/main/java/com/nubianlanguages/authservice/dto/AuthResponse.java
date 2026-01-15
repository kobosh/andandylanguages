package com.nubianlanguages.authservice.dto;

import lombok.Getter;

@Getter
public class AuthResponse {
    private final String token;

    public AuthResponse(String token) {
        System.out.println("in Authresponse");
        this.token = token;
    }

}
