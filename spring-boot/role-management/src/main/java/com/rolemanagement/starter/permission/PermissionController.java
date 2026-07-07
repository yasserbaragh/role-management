package com.rolemanagement.starter.permission;

import com.rolemanagement.starter.permission.dto.PermissionDto;
import com.rolemanagement.starter.permission.dto.PermissionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public PermissionDto create(@RequestBody PermissionRequest request) {
        return PermissionDto.from(permissionService.create(request));
    }

    @PatchMapping("/{id}")
    public PermissionDto update(@PathVariable Long id, @RequestBody PermissionRequest request) {
        return PermissionDto.from(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        permissionService.delete(id);
    }
}