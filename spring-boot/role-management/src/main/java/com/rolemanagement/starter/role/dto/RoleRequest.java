package com.rolemanagement.starter.role.dto;

import java.util.Set;

public record RoleRequest(
        String name,
        Set<String> permissionKeys
) {
}