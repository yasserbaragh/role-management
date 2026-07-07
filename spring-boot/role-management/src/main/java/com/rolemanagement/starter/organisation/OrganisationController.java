package com.rolemanagement.starter.organisation;

import com.rolemanagement.starter.organisation.dto.OrganisationDto;
import com.rolemanagement.starter.organisation.dto.OrganisationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganisationController {

    private final OrganisationService organisationService;

    @GetMapping
    public java.util.List<OrganisationDto> getAll() {
        return organisationService.getAll().stream().map(OrganisationDto::from).toList();
    }

    @GetMapping("/{id}")
    public OrganisationDto getById(@PathVariable Long id) {
        return OrganisationDto.from(organisationService.getById(id));
    }

    @PostMapping
    public OrganisationDto create(@RequestBody OrganisationRequest request) {
        return OrganisationDto.from(organisationService.create(request));
    }

    @PatchMapping("/{id}")
    public OrganisationDto update(@PathVariable Long id, @RequestBody OrganisationRequest request) {
        return OrganisationDto.from(organisationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        organisationService.delete(id);
    }
}