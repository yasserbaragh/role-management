package com.rolemanagement.starter.organisation;

import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.organisation.dto.OrganisationRequest;
import com.rolemanagement.starter.organisationMemberhsip.OrganisationMembershipService;
import com.rolemanagement.starter.role.Role;
import com.rolemanagement.starter.role.RoleService;
import com.rolemanagement.starter.userTable.UserService;
import com.rolemanagement.starter.userTable.UserTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganisationService {
    private final OrganisationRepository organisationRepository;
    private final OrganisationMembershipService organisationMembershipService;
    private final RoleService roleService;
    private final UserService userService;

    public Organisation getById(Long id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Organisation not found: " + id));
    }

    public Organisation create(OrganisationRequest request, String userEmail) {
        Organisation organisation = organisationRepository.save(
                Organisation.builder().name(request.name()).type(request.type()).build()
        );
        Role role = roleService.createDefaultRole(organisation.getId(), "Admin");
        UserTable user = userService.getByEmail(userEmail);
        organisationMembershipService.assignRole(organisation.getId(), user.getId(), role.getId());
        return organisation;
    }

    public Organisation selectOrganisation(Long organisationId, String userEmail) {
        UserTable user = userService.getByEmail(userEmail);
        if (!organisationMembershipService.userBelongsToOrganisation(userEmail, organisationId)) {
            throw new NotFoundException("User is not a member of the organisation: " + organisationId);
        }

        return getById(organisationId);
    }

}