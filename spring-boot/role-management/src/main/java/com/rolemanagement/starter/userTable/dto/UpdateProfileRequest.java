package com.rolemanagement.starter.userTable.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank
        String fullName
) {
}
