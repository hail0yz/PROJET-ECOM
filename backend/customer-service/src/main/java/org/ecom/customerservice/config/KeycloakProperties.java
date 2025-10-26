package org.ecom.customerservice.config;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import org.hibernate.validator.constraints.URL;

@ConfigurationProperties(prefix = "keycloak")
@Validated
public record KeycloakProperties(
    @NotBlank String realm,
    @URL String url,
    @NotBlank String clientId,
    @NotBlank String clientSecret
) {
}