package com.rolemanagement.starter.userTable.dto;

public record LoginResponse(
        String token,
        String email,
        String fullName
) {
}