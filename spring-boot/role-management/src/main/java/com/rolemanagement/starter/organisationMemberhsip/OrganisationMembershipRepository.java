package com.rolemanagement.starter.organisationMemberhsip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganisationMembershipRepository extends JpaRepository<OrganisationMembership, Long> {

    List<OrganisationMembership> findByOrganisationId(Long organisationId);

    Optional<OrganisationMembership> findByUserIdAndOrganisationId(Long userId, Long organisationId);

    Optional<OrganisationMembership> findByUserEmailAndOrganisationId(String email, Long organisationId);

    boolean existsByRoleId(Long roleId);
}