package org.ecom.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
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
                .requestMatchers("/api/v1/carts/user/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/carts").hasAuthority("ROLE_USER")
                .requestMatchers("/api/v1/carts/current", "/api/v1/carts/current/**").hasAuthority("ROLE_USER")
                .requestMatchers(HttpMethod.GET, "/api/v1/carts/{cartId}").hasAuthority("ROLE_USER")
                .requestMatchers(HttpMethod.POST, "/api/v1/carts/{cartId}/items").hasAuthority("ROLE_USER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/carts/{cartId}/items").hasAuthority("ROLE_USER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/carts/{cartId}/items/{bookId}").hasAuthority("ROLE_USER")
                .requestMatchers(HttpMethod.POST, "/api/v1/carts/{cartId}/clear").hasAuthority("ROLE_USER")
                .anyRequest().authenticated());

        http.oauth2ResourceServer(c -> c.jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakAuthenticationConverter())));

        return http.build();
    }

}
