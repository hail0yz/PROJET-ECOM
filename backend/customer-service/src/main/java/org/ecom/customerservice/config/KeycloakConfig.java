package org.ecom.customerservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    private final KeycloakProperties properties;

    @Bean
    public Keycloak keycloak() {
        log.info("Configuring Keycloak with server URL: {}, realm: {}, clientId: {}",
                properties.url(), properties.realm(), properties.clientId());
        return KeycloakBuilder.builder()
                .serverUrl(properties.url())
                .realm(properties.realm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(properties.clientId())
                .clientSecret(properties.clientSecret())
                .build();
    }

}
