package com.rolemanagement.starter.commun;

import com.rolemanagement.starter.organisationMemberhsip.OrganisationMembership;
import com.rolemanagement.starter.organisationMemberhsip.OrganisationMembershipRepository;
import com.rolemanagement.starter.organisationMemberhsip.OrganisationMembershipService;
import com.rolemanagement.starter.permission.Permission;
import com.rolemanagement.starter.permission.PermissionRepository;
import com.rolemanagement.starter.role.Role;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrganisationInterceptor implements HandlerInterceptor {
    private final OrganisationMembershipService organisationMembershipService;
    private final OrganisationMembershipRepository organisationMembershipRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        System.out.println("Organisation Interceptor preHandle");
        if (request.getCookies() != null) {
            System.out.println("Organisation Interceptor preHandle cookies");
            for (Cookie cookie : request.getCookies()) {
                System.out.println(cookie.getName());
                if ("organisationId".equals(cookie.getName())) {
                    System.out.println(cookie.getValue());
                    try {
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth == null || !auth.isAuthenticated()) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            return false;
                        }
                        String userEmail = auth.getName();
                        Long organisationId = Long.parseLong(cookie.getValue());
                        if (!organisationMembershipService.userBelongsToOrganisation(userEmail, organisationId)) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            return false;
                        }
                        OrganisationMembership membership = organisationMembershipRepository.findByUserEmailAndOrganisationId(userEmail, organisationId)
                                .orElseThrow(() -> new RuntimeException("User is not a member of the organisation"));
                        Role role = membership.getRole();
                        Collection<Permission> permissions = role.isSystemRole()
                                ? permissionRepository.findAll()
                                : role.getPermissions();
                        List<SimpleGrantedAuthority> authorities = permissions.stream()
                                .map(permission -> new SimpleGrantedAuthority(permission.getKey()))
                                .toList();
                        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities);
                        newAuth.setDetails(auth.getDetails());
                        SecurityContextHolder.getContext().setAuthentication(newAuth);
                        OrganisationContextHolder.set(organisationId);
                    } catch (NumberFormatException ignored) {
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        OrganisationContextHolder.clear();
    }
}
