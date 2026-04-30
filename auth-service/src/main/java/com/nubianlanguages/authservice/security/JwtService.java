package com.nubianlanguages.authservice.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;


import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final RSAPrivateKey privateKey;
    private final JwtKeyLoader keyLoader;

    public JwtService(JwtKeyLoader keyLoader) {
        this.keyLoader = keyLoader;

        try {
            this.privateKey = keyLoader.getRsaKey().toRSAPrivateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWT private key", e);
        }
    }

    public String generateToken(String userId, long expirationMs, String fullName) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plusMillis(expirationMs)))
                    .claim("fullName", fullName)
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(keyLoader.getRsaKey().getKeyID())
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);

            jwt.sign(new RSASSASigner(privateKey));

            return jwt.serialize();

        } catch (Exception e) {
            throw new RuntimeException("JWT signing failed", e);
        }
    }
}
/*
@Service
public class JwtService {

    private final PrivateKey privateKey;
    RSAPrivateKey privateKey =
            jwtKeyLoader.getRsaKey().toRSAPrivateKey();
    public JwtService(JwtKeyLoader keyLoader) {
        this.privateKey = keyLoader.loadPrivateKey();
    }

    public String generateToken(String userId, long expirationMs,String fullname) {

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
}*/


