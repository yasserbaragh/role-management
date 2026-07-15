package com.rolemanagement.starter.role;

import com.rolemanagement.starter.commun.OrganisationContextHolder;
import com.rolemanagement.starter.role.dto.RoleDto;
import com.rolemanagement.starter.role.dto.RoleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;


    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    public List<RoleDto> getByOrganisation() {
        Long organisationId = OrganisationContextHolder.get();
        return roleService.getByOrganisation(organisationId).stream().map(RoleDto::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_ROLES')")
    public RoleDto getById(@PathVariable Long id) {
        return RoleDto.from(roleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EDIT_ROLES')")
    public RoleDto create(@RequestBody RoleRequest request) {
        Long organisationId = OrganisationContextHolder.get();
        return RoleDto.from(roleService.create(organisationId, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_ROLES')")
    public RoleDto update(@PathVariable Long id, @RequestBody RoleRequest request) {
        return RoleDto.from(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_ROLES')")
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}