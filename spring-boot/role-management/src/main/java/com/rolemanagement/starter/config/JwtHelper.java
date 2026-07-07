package com.rolemanagement.starter.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parserBuilder;

@Component
public class JwtHelper {
    private final Key secretKey;
    private final long expirationTime;

    public JwtHelper(JwtConfig config) {
        this.secretKey = Keys.hmacShaKeyFor(config.getSecret().getBytes());
        this.expirationTime = config.getExpirationTime();
    }

    public String generateToken(Authentication authentication) {
        String email = authentication.getName();
        long now = System.currentTimeMillis();
        return builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaimsFromToken(String token) {
        return parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }
}
