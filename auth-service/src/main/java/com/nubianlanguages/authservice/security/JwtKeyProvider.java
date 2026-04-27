package com.nubianlanguages.authservice.security;

import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;

@Component
public class JwtKeyProvider {

    private final RSAKey rsaKey;

    public JwtKeyProvider() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyID("nubian-key")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key", e);
        }
    }

    public RSAKey getRsaKey() {
        return rsaKey;
    }

    public RSAKey getPublicJwk() {
        return rsaKey.toPublicJWK();
    }
}