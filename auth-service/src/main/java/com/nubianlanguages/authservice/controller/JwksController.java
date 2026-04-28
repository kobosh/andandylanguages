package com.nubianlanguages.authservice.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nubianlanguages.authservice.security.JwtKeyLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

/*@RestController
public class JwtController {

    private final JwtKeyProvider jwtKeyProvider;

    public JwtController(JwtKeyProvider jwtKeyProvider) {
        this.jwtKeyProvider = jwtKeyProvider;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return new JWKSet(jwtKeyProvider.getPublicJwk()).toJSONObject();
    }
}*/
@RestController
public class JwksController {

    private final RSAPublicKey publicKey;

    public JwksController(JwtKeyLoader keyLoader) {
        this.publicKey = (RSAPublicKey) keyLoader.loadPublicKey();
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        RSAKey jwk = new RSAKey.Builder(publicKey)
                .keyID("auth-key-1")
                .build();

        return new JWKSet(jwk).toJSONObject();
    }
}