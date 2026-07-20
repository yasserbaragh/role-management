package com.rolemanagement.starter.organisation;

import com.rolemanagement.starter.common.exception.ForbiddenException;
import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.organisation.dto.OrganisationRequest;
import com.rolemanagement.starter.organisationInvitation.OrganisationInvitationRepository;
import com.rolemanagement.starter.organisationMemberhsip.OrganisationMembershipService;
import com.rolemanagement.starter.role.Role;
import com.rolemanagement.starter.role.RoleService;
import com.rolemanagement.starter.userTable.UserService;
import com.rolemanagement.starter.userTable.UserTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganisationService {
    private final OrganisationRepository organisationRepository;
    private final OrganisationMembershipService organisationMembershipService;
    private final OrganisationInvitationRepository organisationInvitationRepository;
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
        organisationMembershipService.assignOwner(organisation.getId(), user.getId(), role.getId());
        return organisation;
    }

    public Organisation selectOrganisation(Long organisationId, String userEmail) {
        UserTable user = userService.getByEmail(userEmail);
        if (!organisationMembershipService.userBelongsToOrganisation(userEmail, organisationId)) {
            throw new NotFoundException("User is not a member of the organisation: " + organisationId);
        }

        return getById(organisationId);
    }

    public Organisation update(Long organisationId, OrganisationRequest request, String userEmail) {
        requireOwner(organisationId, userEmail);
        Organisation organisation = getById(organisationId);
        organisation.setName(request.name());
        organisation.setType(request.type());
        return organisationRepository.save(organisation);
    }

    @Transactional
    public void delete(Long organisationId, String userEmail) {
        requireOwner(organisationId, userEmail);
        Organisation organisation = getById(organisationId);
        organisationInvitationRepository.deleteAll(organisationInvitationRepository.findByOrganisationId(organisationId));
        organisationMembershipService.deleteAllByOrganisation(organisationId);
        roleService.deleteAllByOrganisation(organisationId);
        organisationRepository.delete(organisation);
    }

    private void requireOwner(Long organisationId, String userEmail) {
        if (!organisationMembershipService.isOwner(userEmail, organisationId)) {
            throw new ForbiddenException("Only the organisation owner can perform this action");
        }
    }

}