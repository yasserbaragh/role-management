package com.rolemanagement.starter.organisationInvitation;

import com.rolemanagement.starter.organisation.Organisation;
import com.rolemanagement.starter.organisationInvitation.enums.InvitationStatus;
import com.rolemanagement.starter.organisationInvitation.enums.InvitationType;
import com.rolemanagement.starter.role.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationInvitation {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Organisation organisation;

    @Enumerated(EnumType.STRING)
    private InvitationType type;

    private String invitedEmail;

    @ManyToOne
    private Role assignedRole;

    private String token;
    private Integer maxUses;
    private Integer usesCount;

    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;
}
