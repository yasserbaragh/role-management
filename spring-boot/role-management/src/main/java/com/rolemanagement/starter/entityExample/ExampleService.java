package com.rolemanagement.starter.entityExample;

import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.entityExample.dto.ExampleRequest;
import com.rolemanagement.starter.organisation.Organisation;
import com.rolemanagement.starter.organisation.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository exampleRepository;
    private final OrganisationRepository organisationRepository;

    public Example getById(Long organisationId, Long id) {
        Example example = exampleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Example not found: " + id));
        if (!example.getOrganisation().getId().equals(organisationId)) {
            throw new NotFoundException("Example not found: " + id);
        }
        return example;
    }

    public List<Example> getByOrganisation(Long organisationId) {
        return exampleRepository.findByOrganisationId(organisationId);
    }

    public Example create(Long organisationId, ExampleRequest request) {
        Organisation organisation = organisationRepository.getReferenceById(organisationId);
        Example example = Example.builder()
                .name(request.name())
                .organisation(organisation)
                .build();
        return exampleRepository.save(example);
    }

    public Example update(Long organisationId, Long id, ExampleRequest request) {
        Example example = getById(organisationId, id);
        example.setName(request.name());
        return exampleRepository.save(example);
    }

    public void delete(Long organisationId, Long id) {
        exampleRepository.delete(getById(organisationId, id));
    }
}