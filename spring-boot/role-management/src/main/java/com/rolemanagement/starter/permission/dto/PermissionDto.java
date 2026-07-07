package com.rolemanagement.starter.permission.dto;

public record PermissionDto(
        Long id,
        String key,
        String label
) {
    public static PermissionDto from(com.rolemanagement.starter.permission.Permission permission) {
        return new PermissionDto(permission.getId(), permission.getKey(), permission.getLabel());
    }
}