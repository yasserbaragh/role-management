package com.rolemanagement.starter.userTable.dto;

public record ForgotPasswordResponse(
        String message,
        String resetToken
) {
}
