package com.rolemanagement.starter.organisation;

import com.rolemanagement.starter.organisation.dto.OrganisationDto;
import com.rolemanagement.starter.organisation.dto.OrganisationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganisationController {

    private final OrganisationService organisationService;

    @PostMapping
    public OrganisationDto create(@AuthenticationPrincipal UserDetails userDetails, @RequestBody OrganisationRequest request) {
        return OrganisationDto.from(organisationService.create(request, userDetails.getUsername()));

    }

    @PostMapping("/select/{organisationId}")
    public String selectOrganisation(@PathVariable Long organisationId, @AuthenticationPrincipal UserDetails userDetails) {
        organisationService.selectOrganisation(organisationId, userDetails.getUsername());
        ResponseCookie cookie = ResponseCookie.from("organisationId", organisationId.toString())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();
        return "Organisation selected successfully." + " Cookie set: " + cookie.toString();
    }

}