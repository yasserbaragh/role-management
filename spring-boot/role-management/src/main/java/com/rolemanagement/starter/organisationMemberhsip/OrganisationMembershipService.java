package com.rolemanagement.starter.organisationMemberhsip;

import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.organisation.Organisation;
import com.rolemanagement.starter.organisation.OrganisationRepository;
import com.rolemanagement.starter.role.Role;
import com.rolemanagement.starter.role.RoleRepository;
import com.rolemanagement.starter.userTable.UserService;
import com.rolemanagement.starter.userTable.UserTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganisationMembershipService {

    private final OrganisationMembershipRepository organisationMembershipRepository;
    private final OrganisationRepository organisationRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;

    public List<OrganisationMembership> getByOrganisation(Long organisationId) {
        return organisationMembershipRepository.findByOrganisationId(organisationId);
    }

    public OrganisationMembership assignRole(Long organisationId, Long userId, Long roleId) {
        Organisation organisation = organisationRepository.getReferenceById(organisationId);
        UserTable user = userService.getById(userId);
        Role role = roleRepository.getReferenceById(roleId);

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

    public boolean userHasRoleInOrganisation(String userEmail, Long organisationId, String roleName) {
        return organisationMembershipRepository.findByUserEmailAndOrganisationId(userEmail, organisationId)
                .map(membership -> membership.getRole().getName().equals(roleName))
                .orElse(false);
    }

    public boolean userBelongsToOrganisation(String userEmail, Long organisationId) {
        return organisationMembershipRepository.findByUserEmailAndOrganisationId(userEmail, organisationId).isPresent();
    }
}