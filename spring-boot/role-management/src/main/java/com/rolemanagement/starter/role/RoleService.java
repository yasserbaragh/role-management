package com.rolemanagement.starter.role;

import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.organisation.Organisation;
import com.rolemanagement.starter.organisation.OrganisationService;
import com.rolemanagement.starter.permission.Permission;
import com.rolemanagement.starter.permission.PermissionRepository;
import com.rolemanagement.starter.role.dto.RoleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final OrganisationService organisationService;

    public Role getById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found: " + id));
    }

    public List<Role> getByOrganisation(Long organisationId) {
        return roleRepository.findByOrganisationId(organisationId);
    }

    public Role create(Long organisationId, RoleRequest request) {
        Organisation organisation = organisationService.getById(organisationId);
        Role role = Role.builder()
                .name(request.name())
                .organisation(organisation)
                .permissions(resolvePermissions(request.permissionKeys()))
                .build();
        return roleRepository.save(role);
    }

    public Role update(Long id, RoleRequest request) {
        Role role = getById(id);
        role.setName(request.name());
        role.setPermissions(resolvePermissions(request.permissionKeys()));
        return roleRepository.save(role);
    }

    public void delete(Long id) {
        roleRepository.delete(getById(id));
    }

    private Set<Permission> resolvePermissions(Set<String> keys) {
        if (keys == null) {
            return Set.of();
        }
        return keys.stream()
                .map(key -> permissionRepository.findByKey(key)
                        .orElseThrow(() -> new NotFoundException("Permission not found: " + key)))
                .collect(Collectors.toSet());
    }
}