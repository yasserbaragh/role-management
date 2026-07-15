package com.rolemanagement.starter.entityExample.dto;

import com.rolemanagement.starter.entityExample.Example;

public record ExampleDto(
        Long id,
        String name,
        Long organisationId
) {
    public static ExampleDto from(Example example) {
        return new ExampleDto(example.getId(), example.getName(), example.getOrganisation().getId());
    }
}