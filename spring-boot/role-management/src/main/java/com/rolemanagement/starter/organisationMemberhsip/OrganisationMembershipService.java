package com.rolemanagement.starter.organisationMemberhsip;

import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.organisation.Organisation;
import com.rolemanagement.starter.organisation.OrganisationService;
import com.rolemanagement.starter.role.Role;
import com.rolemanagement.starter.role.RoleService;
import com.rolemanagement.starter.userTable.UserService;
import com.rolemanagement.starter.userTable.UserTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganisationMembershipService {

    private final OrganisationMembershipRepository organisationMembershipRepository;
    private final OrganisationService organisationService;
    private final UserService userService;
    private final RoleService roleService;

    public List<OrganisationMembership> getByOrganisation(Long organisationId) {
        return organisationMembershipRepository.findByOrganisationId(organisationId);
    }

    public OrganisationMembership assignRole(Long organisationId, Long userId, Long roleId) {
        Organisation organisation = organisationService.getById(organisationId);
        UserTable user = userService.getById(userId);
        Role role = roleService.getById(roleId);

        OrganisationMembership membership = organisationMembershipRepository
                .findByUserIdAndOrganisationId(userId, organisationId)
                .orElseGet(() -> OrganisationMembership.builder()
                        .user(user)
                        .organisation(organisation)
                        .build());

        membership.setRole(role);
        return organisationMembershipRepository.save(membership);
    }

    public void revokeMembership(Long organisationId, Long userId) {
        OrganisationMembership membership = organisationMembershipRepository
                .findByUserIdAndOrganisationId(userId, organisationId)
                .orElseThrow(() -> new NotFoundException("Membership not found for user " + userId + " in organisation " + organisationId));
        organisationMembershipRepository.delete(membership);
    }
}