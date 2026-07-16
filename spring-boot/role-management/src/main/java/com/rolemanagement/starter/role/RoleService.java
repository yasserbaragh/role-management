package com.rolemanagement.starter.role;

import com.rolemanagement.starter.common.exception.ConflictException;
import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.organisation.Organisation;
import com.rolemanagement.starter.organisation.OrganisationRepository;
import com.rolemanagement.starter.organisationMemberhsip.OrganisationMembershipRepository;
import com.rolemanagement.starter.permission.Permission;
import com.rolemanagement.starter.permission.PermissionRepository;
import com.rolemanagement.starter.role.dto.RoleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final OrganisationRepository organisationRepository;
    private final OrganisationMembershipRepository organisationMembershipRepository;

    public Role getById(Long organisationId, Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found: " + id));
        if (!role.getOrganisation().getId().equals(organisationId)) {
            throw new NotFoundException("Role not found: " + id);
        }
        return role;
    }

    public List<Role> getByOrganisation(Long organisationId) {
        return roleRepository.findByOrganisationId(organisationId);
    }

    public Role create(Long organisationId, RoleRequest request) {
        Organisation organisation = organisationRepository.getReferenceById(organisationId);
        Role role = Role.builder()
                .name(request.name())
                .organisation(organisation)
                .permissions(resolvePermissions(request.permissionKeys()))
                .build();
        return roleRepository.save(role);
    }

    public Role createDefaultRole(Long organisationId, String roleName) {
        Organisation organisation = organisationRepository.getReferenceById(organisationId);
        Role role = Role.builder()
                .name(roleName)
                .isSystemRole(true)
                .organisation(organisation)
                .permissions(Set.of())
                .build();
        return roleRepository.save(role);
    }


    @CacheEvict(cacheNames = "userPermissions", allEntries = true)
    public Role update(Long organisationId, Long id, RoleRequest request) {
        Role role = getById(organisationId, id);
        role.setName(request.name());
        role.setPermissions(resolvePermissions(request.permissionKeys()));
        return roleRepository.save(role);
    }

    public void delete(Long organisationId, Long id) {
        Role role = getById(organisationId, id);
        if (organisationMembershipRepository.existsByRoleId(id)) {
            throw new ConflictException("Cannot delete role: it is still assigned to organisation members");
        }
        roleRepository.delete(role);
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