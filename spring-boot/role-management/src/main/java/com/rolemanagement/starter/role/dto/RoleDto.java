package com.rolemanagement.starter.role.dto;

import com.rolemanagement.starter.permission.Permission;

import java.util.Set;
import java.util.stream.Collectors;

public record RoleDto(
        Long id,
        String name,
        Long organisationId,
        Set<String> permissionKeys
) {
    public static RoleDto from(com.rolemanagement.starter.role.Role role) {
        return new RoleDto(
                role.getId(),
                role.getName(),
                role.getOrganisation().getId(),
                role.getPermissions().stream().map(Permission::getKey).collect(Collectors.toSet())
        );
    }
}