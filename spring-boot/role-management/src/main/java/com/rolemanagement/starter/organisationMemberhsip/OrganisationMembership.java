package com.rolemanagement.starter.organisationMemberhsip;

import com.rolemanagement.starter.organisation.Organisation;
import com.rolemanagement.starter.role.Role;
import com.rolemanagement.starter.userTable.UserTable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "organisation_membership", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "organisation_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganisationMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserTable user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isOwner;

    @Column(updatable = false)
    private Instant joinedAt;

    @PrePersist
    void onCreate() {
        joinedAt = Instant.now();
    }
}
