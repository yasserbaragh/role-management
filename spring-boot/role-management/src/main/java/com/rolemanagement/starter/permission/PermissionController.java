package com.rolemanagement.starter.permission;

import com.rolemanagement.starter.permission.dto.PermissionDto;
import com.rolemanagement.starter.permission.dto.PermissionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @PreAuthorize("hasAuthority('EDIT_PERMISSIONS')")
    public PermissionDto create(@RequestBody PermissionRequest request) {
        return PermissionDto.from(permissionService.create(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_PERMISSIONS')")
    public PermissionDto update(@PathVariable Long id, @RequestBody PermissionRequest request) {
        return PermissionDto.from(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_PERMISSIONS')")
    public void delete(@PathVariable Long id) {
        permissionService.delete(id);
    }
}