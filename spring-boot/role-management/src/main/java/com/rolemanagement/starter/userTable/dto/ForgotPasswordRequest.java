package com.rolemanagement.starter.userTable.dto;

import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(
        @Email
        String email
) {
}
