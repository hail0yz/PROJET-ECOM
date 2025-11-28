package org.ecom.customerservice;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class AuthorizationUtils {

    private AuthorizationUtils() {
    }

    public static List<String> getAuthorities(Authentication authentication) {
        List<String> roles;
        if (authentication != null) {
            roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        }
        else {
            roles = List.of();
        }
        return roles;
    }

}
