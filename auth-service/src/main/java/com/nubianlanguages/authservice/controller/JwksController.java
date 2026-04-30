package com.nubianlanguages.authservice.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nubianlanguages.authservice.security.JwtKeyLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;



@RestController
public class JwksController {

    private final JwtKeyLoader jwtKeyLoader;

    public JwksController(JwtKeyLoader jwtKeyLoader) {
        this.jwtKeyLoader = jwtKeyLoader;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return new JWKSet(jwtKeyLoader.getPublicJwk()).toJSONObject();
    }
}

