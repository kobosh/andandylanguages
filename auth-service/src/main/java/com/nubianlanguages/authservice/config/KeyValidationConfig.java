package com.nubianlanguages.authservice.config;

import com.nubianlanguages.authservice.security.JwtKeyLoader;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;


@Configuration
public class KeyValidationConfig {

    @Bean
    ApplicationRunner validateKeysOnStartup(JwtKeyLoader loader) {
        return args -> {
            PrivateKey privateKey = loader.loadPrivateKey();
            PublicKey publicKey = loader.loadPublicKey();

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
        };
    }
}

