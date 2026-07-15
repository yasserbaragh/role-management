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

    public Example getById(Long id) {
        return exampleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Example not found: " + id));
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

    public Example update(Long id, ExampleRequest request) {
        Example example = getById(id);
        example.setName(request.name());
        return exampleRepository.save(example);
    }

    public void delete(Long id) {
        exampleRepository.delete(getById(id));
    }
}