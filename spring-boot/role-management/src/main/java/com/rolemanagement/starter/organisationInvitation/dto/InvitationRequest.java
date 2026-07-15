package com.rolemanagement.starter.organisationInvitation.dto;

public record InvitationRequest(
        String email,
        Long roleId
) {
}
