package com.rolemanagement.starter.commun;

import com.rolemanagement.starter.common.exception.NotFoundException;
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

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class OrganisationInterceptor implements HandlerInterceptor {
    private final UserPermissionService userPermissionService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("organisationId".equals(cookie.getName())) {
                    try {
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth == null || !auth.isAuthenticated()) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            return false;
                        }
                        String userEmail = auth.getName();
                        Long organisationId = Long.parseLong(cookie.getValue());

                        Set<String> permissionKeys;
                        try {
                            permissionKeys = userPermissionService.getUserPermissions(userEmail, organisationId);
                        } catch (NotFoundException e) {
                            // organisationId cookie doesn't match a membership for this user
                            // (stale/foreign org, e.g. leftover from another account). Don't block
                            // the whole request - just skip granting org-scoped authorities.
                            // Endpoints that actually require org context/permissions will still
                            // reject it on their own (via @PreAuthorize or OrganisationContextHolder).
                            break;
                        }

                        List<SimpleGrantedAuthority> authorities = permissionKeys.stream()
                                .map(SimpleGrantedAuthority::new)
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
