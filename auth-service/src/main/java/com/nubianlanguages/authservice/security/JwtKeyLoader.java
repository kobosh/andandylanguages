package com.nubianlanguages.authservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtKeyLoader {

    @Value("${jwt.private-key-location}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-location}")
    private Resource publicKeyResource;

    public PrivateKey loadPrivateKey() {
        try (InputStream is = privateKeyResource.getInputStream()) {
            String key = new String(is.readAllBytes())
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA").generatePrivate(spec);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load private key", e);
        }
    }

    public PublicKey loadPublicKey() {
        try (InputStream is = publicKeyResource.getInputStream()) {
            String key = new String(is.readAllBytes())
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA").generatePublic(spec);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load public key", e);
        }
    }
}
