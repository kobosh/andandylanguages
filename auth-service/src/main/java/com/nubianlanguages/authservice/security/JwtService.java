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

    public String generateToken(String userId, long expirationMs) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .issueTime(new Date())
                    .expirationTime(
                            Date.from(Instant.now().plusMillis(expirationMs))
                    )
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


/*@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private Key getSignInKey() {
        System.out.println("in jwts.getSignInKey");
        // Secret MUST be Base64 encoded
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);    }

    public String generateToken(String email) {
        System.out.println("in Jwtservice.generateToken");
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24) // 24h
                )
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}*/
