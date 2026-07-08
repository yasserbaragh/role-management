package com.rolemanagement.starter.userTable.dto;

public record LoginRequest(
        String email,
        String password
) {
}
