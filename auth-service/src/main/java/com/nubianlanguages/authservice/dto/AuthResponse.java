package com.nubianlanguages.authservice.dto;

public record AuthResponse(String token) {
    public AuthResponse {
        System.out.println("in Authresponse");
    }

}
