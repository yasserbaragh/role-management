package com.rolemanagement.starter.commun;

import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.organisationMemberhsip.OrganisationMembership;
import com.rolemanagement.starter.organisationMemberhsip.OrganisationMembershipRepository;
import com.rolemanagement.starter.permission.Permission;
import com.rolemanagement.starter.permission.PermissionRepository;
import com.rolemanagement.starter.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPermissionService {

    private final OrganisationMembershipRepository organisationMembershipRepository;
    private final PermissionRepository permissionRepository;

    @Cacheable(cacheNames = "userPermissions", key = "#userEmail + ':' + #organisationId")
    public Set<String> getUserPermissions(String userEmail, Long organisationId) {
        OrganisationMembership membership = organisationMembershipRepository
                .findByUserEmailAndOrganisationId(userEmail, organisationId)
                .orElseThrow(() -> new NotFoundException(
                        "User " + userEmail + " is not a member of organisation " + organisationId));

        Role role = membership.getRole();
        Collection<Permission> permissions = role.isSystemRole()
                ? permissionRepository.findAll()
                : role.getPermissions();
        return permissions.stream().map(Permission::getKey).collect(Collectors.toSet());
    }
}
