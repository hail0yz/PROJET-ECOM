package org.ecom.customerservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(c -> c.requestMatchers(
                        "/actuator/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs*/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/register").permitAll()
                .requestMatchers("/api/v1/customers/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER"));

        http.oauth2ResourceServer(c -> c.jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakAuthenticationConverter())));

        return http.build();
    }

}
