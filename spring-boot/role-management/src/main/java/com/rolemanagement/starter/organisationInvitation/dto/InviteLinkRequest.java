package com.rolemanagement.starter.organisationInvitation.dto;

public record InviteLinkRequest(
        Long roleId,
        Integer maxUses,
        Long expiresInHours
) {
}