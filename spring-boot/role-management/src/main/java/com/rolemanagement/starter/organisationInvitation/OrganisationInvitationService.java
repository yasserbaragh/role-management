package com.rolemanagement.starter.organisationInvitation;

import com.rolemanagement.starter.commun.OrganisationContextHolder;
import com.rolemanagement.starter.config.EmailVerificationConfig;
import com.rolemanagement.starter.email.EmailService;
import com.rolemanagement.starter.organisation.Organisation;
import com.rolemanagement.starter.organisation.OrganisationService;
import com.rolemanagement.starter.organisationInvitation.dto.InvitationRequest;
import com.rolemanagement.starter.organisationInvitation.dto.InviteLinkRequest;
import com.rolemanagement.starter.organisationInvitation.dto.JoinLinkRequest;
import com.rolemanagement.starter.organisationInvitation.enums.InvitationStatus;
import com.rolemanagement.starter.organisationInvitation.enums.InvitationType;
import com.rolemanagement.starter.organisationMemberhsip.OrganisationMembership;
import com.rolemanagement.starter.organisationMemberhsip.OrganisationMembershipService;
import com.rolemanagement.starter.role.Role;
import com.rolemanagement.starter.role.RoleService;
import com.rolemanagement.starter.userTable.UserRepository;
import com.rolemanagement.starter.userTable.UserTable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class OrganisationInvitationService {
    private final OrganisationService organisationService;
    private final RoleService roleService;
    private final OrganisationInvitationRepository organisationInvitationRepository;
    private final OrganisationMembershipService organisationMembershipService;
    private final OrganisationInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final EmailVerificationConfig emailVerificationConfig;
    private final EmailService emailService;

    @Transactional
    public OrganisationInvitation inviteByEmail(InvitationRequest request, String inviterEmail) {
        Long organisationId = OrganisationContextHolder.get();
        Organisation org = organisationService.getById(organisationId);
        Role role = roleService.getById(organisationId, request.roleId());

        OrganisationInvitation invitation = OrganisationInvitation.builder()
                .organisation(org)
                .type(InvitationType.DIRECT)
                .invitedEmail(request.email())
                .assignedRole(role)
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        invitation = organisationInvitationRepository.save(invitation);

        if (emailVerificationConfig.isEnabled()) {
            emailService.sendInvitationEmail(request.email(), org.getName(), invitation.getId());
        }
        return invitation;
    }

    @Transactional
    public OrganisationMembership acceptInvitation(Long invitationId, String userEmail) {
        OrganisationInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is no longer valid");
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new IllegalStateException("Invitation has expired");
        }
        if (!invitation.getInvitedEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("This invitation was not sent to your account");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        UserTable user = userRepository.findByEmail(userEmail).orElseThrow(() -> new EntityNotFoundException("User not found"));

        return organisationMembershipService.assignRole(
                invitation.getOrganisation().getId(), user.getId(), invitation.getAssignedRole().getId()
        );
    }

    @Transactional
    public OrganisationInvitation createInviteLink(InviteLinkRequest request, String inviterEmail) {
        Long organisationId = OrganisationContextHolder.get();
        Organisation org = organisationService.getById(organisationId);
        Role role = roleService.getById(organisationId, request.roleId());

        OrganisationInvitation invitation = OrganisationInvitation.builder()
                .organisation(org)
                .type(InvitationType.LINK)
                .assignedRole(role)
                .token(generateSecureToken())
                .maxUses(request.maxUses())
                .usesCount(0)
                .status(InvitationStatus.PENDING)
                .expiresAt(request.expiresInHours() != null ? LocalDateTime.now().plusHours(request.expiresInHours()) : null)
                .createdAt(LocalDateTime.now())
                .build();

        return invitationRepository.save(invitation);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[24];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Transactional
    public OrganisationMembership joinViaLink(String invitationToken, JoinLinkRequest request, String userEmail) {
        OrganisationInvitation invitation = invitationRepository.findByToken(invitationToken)
                .orElseThrow(() -> new EntityNotFoundException("Invalid invite link"));

        if (invitation.getStatus() == InvitationStatus.REVOKED) {
            throw new IllegalStateException("This invite link has been revoked");
        }
        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This invite link has expired");
        }
        if (invitation.getMaxUses() != null && invitation.getUsesCount() >= invitation.getMaxUses()) {
            throw new IllegalStateException("This invite link has reached its usage limit");
        }

        invitation.setUsesCount(invitation.getUsesCount() + 1);
        invitationRepository.save(invitation);

        UserTable user = userRepository.findByEmail(userEmail).orElseThrow(() -> new EntityNotFoundException("User not found"));

        return organisationMembershipService.assignRole(
                invitation.getOrganisation().getId(), user.getId(), invitation.getAssignedRole().getId()
        );
    }
}
