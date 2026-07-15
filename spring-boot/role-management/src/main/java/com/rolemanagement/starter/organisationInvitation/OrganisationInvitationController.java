package com.rolemanagement.starter.organisationInvitation;

import com.rolemanagement.starter.organisationInvitation.dto.InvitationRequest;
import com.rolemanagement.starter.organisationInvitation.dto.InvitationResponse;
import com.rolemanagement.starter.organisationInvitation.dto.InviteLinkRequest;
import com.rolemanagement.starter.organisationInvitation.dto.JoinLinkRequest;
import com.rolemanagement.starter.organisationMemberhsip.dto.OrganisationMembershipDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class OrganisationInvitationController {

    private final OrganisationInvitationService organisationInvitationService;

    @PostMapping
    @PreAuthorize("hasAuthority('EDIT_MEMBERSHIPS')")
    public InvitationResponse inviteByEmail(
            @RequestBody InvitationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return InvitationResponse.from(
                organisationInvitationService.inviteByEmail(request, userDetails.getUsername())
        );
    }

    @PostMapping("/link")
    @PreAuthorize("hasAuthority('EDIT_MEMBERSHIPS')")
    public InvitationResponse createInviteLink(
            @RequestBody InviteLinkRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return InvitationResponse.from(
                organisationInvitationService.createInviteLink(request, userDetails.getUsername())
        );
    }

    @PostMapping("/{invitationId}/accept")
    public OrganisationMembershipDto acceptInvitation(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return OrganisationMembershipDto.from(
                organisationInvitationService.acceptInvitation(invitationId, userDetails.getUsername())
        );
    }

    @PostMapping("/join/{invitationToken}")
    public OrganisationMembershipDto joinViaLink(
            @PathVariable String invitationToken,
            @RequestBody JoinLinkRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return OrganisationMembershipDto.from(
                organisationInvitationService.joinViaLink(invitationToken, request, userDetails.getUsername())
        );
    }
}