package com.rolemanagement.starter.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {

    List<Role> findByOrganisationId(Long organisationId);

    List<Role> findByPermissions_Id(Long permissionId);
}