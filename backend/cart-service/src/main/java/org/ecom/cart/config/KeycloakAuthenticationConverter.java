package org.ecom.cart.config;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class KeycloakAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        var scope = new JwtGrantedAuthoritiesConverter().convert(source).stream();
        var roles = Stream.concat(scope, extractRealmAccessAuthorities(source))
                .collect(Collectors.toSet());

        return new JwtAuthenticationToken(source, roles);
    }

    private Stream<GrantedAuthority> extractRealmAccessAuthorities(Jwt jwt) {
        var realmAccess = new HashMap<>(jwt.getClaim("realm_access"));
        return ((List<String>) realmAccess.get("roles")).stream()
                .map(this::mapRoleToAuthority);
    }

    private GrantedAuthority mapRoleToAuthority(String role) {
        return new SimpleGrantedAuthority("ROLE_" + role.replace("-", "_"));
    }

}