package com.rolemanagement.starter.organisationMemberhsip;

import com.rolemanagement.starter.organisationMemberhsip.dto.AssignRoleRequest;
import com.rolemanagement.starter.organisationMemberhsip.dto.OrganisationMembershipDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{organisationId}/memberships")
@RequiredArgsConstructor
public class OrganisationMembershipController {

    private final OrganisationMembershipService organisationMembershipService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_MEMBERSHIPS')")
    public List<OrganisationMembershipDto> getByOrganisation(@PathVariable Long organisationId) {
        return organisationMembershipService.getByOrganisation(organisationId).stream()
                .map(OrganisationMembershipDto::from)
                .toList();
    }

    @PostMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('EDIT_MEMBERSHIPS')")
    public OrganisationMembershipDto assignRole(@PathVariable Long organisationId,
                                                 @PathVariable Long userId,
                                                 @RequestBody AssignRoleRequest request) {
        return OrganisationMembershipDto.from(
                organisationMembershipService.assignRole(organisationId, userId, request.roleId())
        );
    }
}