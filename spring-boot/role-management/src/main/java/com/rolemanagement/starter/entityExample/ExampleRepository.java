package com.rolemanagement.starter.entityExample;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExampleRepository extends JpaRepository<Example, Long> {
    List<Example> findByOrganisationId(Long organisationId);
}