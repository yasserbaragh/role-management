package com.rolemanagement.starter.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class EmailVerificationConfig {
    private final boolean enabled;

    public EmailVerificationConfig(@Value("${app.email-verification.enabled:false}") boolean enabled) {
        this.enabled = enabled;
    }
}
