package com.rolemanagement.starter.permission;

import com.rolemanagement.starter.common.exception.ConflictException;
import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.permission.dto.PermissionRequest;
import com.rolemanagement.starter.role.Role;
import com.rolemanagement.starter.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public Permission getById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission not found: " + id));
    }

    public List<Permission> getAll() {
        return permissionRepository.findAll();
    }

    public Permission create(PermissionRequest request) {
        permissionRepository.findByKey(request.key()).ifPresent(p -> {
            throw new ConflictException("Permission key already exists: " + request.key());
        });
        Permission permission = Permission.builder()
                .key(request.key())
                .label(request.label())
                .build();
        return permissionRepository.save(permission);
    }

    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public Permission update(Long id, PermissionRequest request) {
        Permission permission = getById(id);
        permission.setKey(request.key());
        permission.setLabel(request.label());
        return permissionRepository.save(permission);
    }

    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public void delete(Long id) {
        Permission permission = getById(id);
        List<Role> rolesWithPermission = roleRepository.findByPermissions_Id(id);
        rolesWithPermission.forEach(role -> role.getPermissions().remove(permission));
        roleRepository.saveAll(rolesWithPermission);
        permissionRepository.delete(permission);
    }
}