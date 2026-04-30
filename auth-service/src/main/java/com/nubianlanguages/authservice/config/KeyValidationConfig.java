package com.nubianlanguages.authservice.config;

import com.nubianlanguages.authservice.security.JwtKeyLoader;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class KeyValidationConfig {

    @Bean
    ApplicationRunner validateKeysOnStartup(JwtKeyLoader loader) {
        return args -> {
            try {
                RSAPrivateKey privateKey = loader.getRsaKey().toRSAPrivateKey();
                RSAPublicKey publicKey = loader.getRsaKey().toRSAPublicKey();

                byte[] testData = "jwt-key-validation".getBytes();

                Signature signer = Signature.getInstance("SHA256withRSA");
                signer.initSign(privateKey);
                signer.update(testData);
                byte[] signature = signer.sign();

                Signature verifier = Signature.getInstance("SHA256withRSA");
                verifier.initVerify(publicKey);
                verifier.update(testData);

                if (!verifier.verify(signature)) {
                    throw new IllegalStateException(
                            "Private/Public key mismatch — JWT signing will fail"
                    );
                }

                System.out.println("✅ RSA key pair validated successfully");

            } catch (Exception e) {
                throw new RuntimeException("JWT key validation failed", e);
            }
        };
    }
}