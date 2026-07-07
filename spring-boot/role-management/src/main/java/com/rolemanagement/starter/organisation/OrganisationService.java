package com.rolemanagement.starter.organisation;

import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.organisation.dto.OrganisationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    public Organisation getById(Long id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Organisation not found: " + id));
    }

    public java.util.List<Organisation> getAll() {
        return organisationRepository.findAll();
    }

    public Organisation create(OrganisationRequest request) {
        Organisation organisation = Organisation.builder()
                .name(request.name())
                .type(request.type())
                .build();
        return organisationRepository.save(organisation);
    }

    public Organisation update(Long id, OrganisationRequest request) {
        Organisation organisation = getById(id);
        organisation.setName(request.name());
        organisation.setType(request.type());
        return organisationRepository.save(organisation);
    }

    public void delete(Long id) {
        organisationRepository.delete(getById(id));
    }
}