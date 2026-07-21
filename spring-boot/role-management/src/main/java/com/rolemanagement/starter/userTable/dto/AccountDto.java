package com.rolemanagement.starter.userTable.dto;

import com.rolemanagement.starter.userTable.UserTable;

public record AccountDto(
        Long id,
        String email,
        String fullName,
        boolean emailVerified
) {
    public static AccountDto from(UserTable user) {
        return new AccountDto(user.getId(), user.getEmail(), user.getFullName(), user.isEmailVerified());
    }
}
