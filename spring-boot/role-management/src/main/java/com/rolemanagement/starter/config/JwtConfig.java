package com.rolemanagement.starter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
public class JwtConfig {
    private final String secret;
    private final long expirationTime;

    public JwtConfig(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationTime) {
        this.secret = secret;
        this.expirationTime = expirationTime;
    }
}
