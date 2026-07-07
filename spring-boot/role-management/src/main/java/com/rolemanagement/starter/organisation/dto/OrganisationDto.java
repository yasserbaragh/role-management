package com.rolemanagement.starter.organisation.dto;

public record OrganisationDto(
        Long id,
        String name,
        String type
) {
    public static OrganisationDto from(com.rolemanagement.starter.organisation.Organisation organisation) {
        return new OrganisationDto(organisation.getId(), organisation.getName(), organisation.getType());
    }
}