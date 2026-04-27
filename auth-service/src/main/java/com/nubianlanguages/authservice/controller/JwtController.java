package com.nubianlanguages.authservice.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nubianlanguages.authservice.security.JwtKeyProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwtController {

    private final JwtKeyProvider jwtKeyProvider;

    public JwtController(JwtKeyProvider jwtKeyProvider) {
        this.jwtKeyProvider = jwtKeyProvider;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return new JWKSet(jwtKeyProvider.getPublicJwk()).toJSONObject();
    }
}