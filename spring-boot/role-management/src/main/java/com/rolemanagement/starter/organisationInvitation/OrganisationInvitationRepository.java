package com.rolemanagement.starter.organisationInvitation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganisationInvitationRepository extends JpaRepository<OrganisationInvitation, Long> {
    public Optional<OrganisationInvitation> findByToken(String token);

    List<OrganisationInvitation> findByOrganisationId(Long organisationId);
}
