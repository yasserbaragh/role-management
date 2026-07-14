package com.rolemanagement.starter.organisation;

import com.rolemanagement.starter.organisation.dto.OrganisationDto;
import com.rolemanagement.starter.organisation.dto.OrganisationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganisationController {

    private final OrganisationService organisationService;

    @PostMapping
    public OrganisationDto create(@AuthenticationPrincipal UserDetails userDetails, @RequestBody OrganisationRequest request) {
        return OrganisationDto.from(organisationService.create(request, userDetails.getUsername()));
    }

}