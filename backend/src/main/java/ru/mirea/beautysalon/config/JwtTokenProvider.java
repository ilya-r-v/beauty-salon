package ru.mirea.beautysalon.config;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import ru.mirea.beautysalon.entity.Client;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:dab6650d-d423-42d6-81b1-8508fd650af2-beauty-salon-secret-key-32b}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
        this.expirationMs = expirationMs;
    }

    public String generateToken(Client client) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(client.getId().toString())
                .claim("phone", client.getPhone())
                .claim("role", client.getRole())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}