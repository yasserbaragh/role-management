package com.rolemanagement.starter.permission;

import com.rolemanagement.starter.common.exception.ConflictException;
import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.permission.dto.PermissionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public Permission getById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission not found: " + id));
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

    public Permission update(Long id, PermissionRequest request) {
        Permission permission = getById(id);
        permission.setKey(request.key());
        permission.setLabel(request.label());
        return permissionRepository.save(permission);
    }

    public void delete(Long id) {
        permissionRepository.delete(getById(id));
    }
}