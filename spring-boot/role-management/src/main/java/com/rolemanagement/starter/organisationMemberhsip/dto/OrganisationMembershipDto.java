package com.rolemanagement.starter.organisationMemberhsip.dto;

public record OrganisationMembershipDto(
        Long id,
        Long userId,
        Long organisationId,
        Long roleId,
        String roleName
) {
    public static OrganisationMembershipDto from(com.rolemanagement.starter.organisationMemberhsip.OrganisationMembership membership) {
        return new OrganisationMembershipDto(
                membership.getId(),
                membership.getUser().getId(),
                membership.getOrganisation().getId(),
                membership.getRole().getId(),
                membership.getRole().getName()
        );
    }
}