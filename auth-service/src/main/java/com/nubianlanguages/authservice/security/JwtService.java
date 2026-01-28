package com.nubianlanguages.authservice.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final PrivateKey privateKey;

    public JwtService(JwtKeyLoader keyLoader) {
        this.privateKey = keyLoader.loadPrivateKey();
    }

    public String generateToken(String userId, long expirationMs,String fullname) {
        System.out.println();
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .issueTime(new Date())
                    .expirationTime(
                            Date.from(Instant.now().plusMillis(expirationMs))
                    )
                    .claim("fullName", fullname)
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.RS256),
                    claims
            );

            jwt.sign(new RSASSASigner(privateKey));
            return jwt.serialize();

        } catch (Exception e) {
            throw new RuntimeException("JWT signing failed", e);
        }
    }
}
/*JwtClaimsSet claims = JwtClaimsSet.builder()
        .subject(userId)
        .claim("fullName", user.getFullname())
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusMillis(expirationMs))
        .build();*/

