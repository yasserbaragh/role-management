package com.rolemanagement.starter.userTable.dto;

import com.rolemanagement.starter.userTable.UserTable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserDto(
        Long id,

        @Email
        String email,

        @NotBlank
        String fullName,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
                message = "Password must be at least 8 characters long and contain an uppercase letter, lowercase letter, number, and special character."
        )
        String password
) {
    public static UserDto from(UserTable user) {
        return new UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getPassword());
    }
}