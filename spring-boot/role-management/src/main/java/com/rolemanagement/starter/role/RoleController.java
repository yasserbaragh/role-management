package com.rolemanagement.starter.role;

import com.rolemanagement.starter.role.dto.RoleDto;
import com.rolemanagement.starter.role.dto.RoleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // TODO: resolve the active organization from the request (e.g. cookie) instead of a query param.
    @GetMapping
    public List<RoleDto> getByOrganisation(@RequestParam Long organisationId) {
        return roleService.getByOrganisation(organisationId).stream().map(RoleDto::from).toList();
    }

    @GetMapping("/{id}")
    public RoleDto getById(@PathVariable Long id) {
        return RoleDto.from(roleService.getById(id));
    }

    @PostMapping
    public RoleDto create(@RequestParam Long organisationId, @RequestBody RoleRequest request) {
        return RoleDto.from(roleService.create(organisationId, request));
    }

    @PatchMapping("/{id}")
    public RoleDto update(@PathVariable Long id, @RequestBody RoleRequest request) {
        return RoleDto.from(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}