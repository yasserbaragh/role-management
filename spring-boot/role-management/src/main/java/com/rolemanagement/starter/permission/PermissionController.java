package com.rolemanagement.starter.permission;

import com.rolemanagement.starter.permission.dto.PermissionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_PERMISSIONS')")
    public List<PermissionDto> getAll() {
        return permissionService.getAll().stream().map(PermissionDto::from).toList();
    }
}