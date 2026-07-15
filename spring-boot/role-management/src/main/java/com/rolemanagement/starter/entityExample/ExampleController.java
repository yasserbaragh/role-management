package com.rolemanagement.starter.entityExample;

import com.rolemanagement.starter.commun.OrganisationContextHolder;
import com.rolemanagement.starter.entityExample.dto.ExampleDto;
import com.rolemanagement.starter.entityExample.dto.ExampleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
public class ExampleController {

    private final ExampleService exampleService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_EXAMPLE')")
    public List<ExampleDto> getByOrganisation() {
        Long organisationId = OrganisationContextHolder.get();
        return exampleService.getByOrganisation(organisationId).stream().map(ExampleDto::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_EXAMPLE')")
    public ExampleDto getById(@PathVariable Long id) {
        return ExampleDto.from(exampleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EDIT_EXAMPLE')")
    public ExampleDto create(@RequestBody ExampleRequest request) {
        Long organisationId = OrganisationContextHolder.get();
        return ExampleDto.from(exampleService.create(organisationId, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_EXAMPLE')")
    public ExampleDto update(@PathVariable Long id, @RequestBody ExampleRequest request) {
        return ExampleDto.from(exampleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_EXAMPLE')")
    public void delete(@PathVariable Long id) {
        exampleService.delete(id);
    }
}
