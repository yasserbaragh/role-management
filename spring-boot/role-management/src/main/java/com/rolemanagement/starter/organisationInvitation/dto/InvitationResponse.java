package com.rolemanagement.starter.organisationInvitation.dto;

import com.rolemanagement.starter.organisationInvitation.OrganisationInvitation;
import com.rolemanagement.starter.organisationInvitation.enums.InvitationStatus;
import com.rolemanagement.starter.organisationInvitation.enums.InvitationType;

import java.time.LocalDateTime;

public record InvitationResponse(
        Long id,
        Long organisationId,
        InvitationType type,
        String invitedEmail,
        Long roleId,
        String roleName,
        String token,
        Integer maxUses,
        Integer usesCount,
        InvitationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static InvitationResponse from(OrganisationInvitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getOrganisation().getId(),
                invitation.getType(),
                invitation.getInvitedEmail(),
                invitation.getAssignedRole().getId(),
                invitation.getAssignedRole().getName(),
                invitation.getToken(),
                invitation.getMaxUses(),
                invitation.getUsesCount(),
                invitation.getStatus(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt()
        );
    }
}
