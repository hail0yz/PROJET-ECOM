package com.ecom.bookService.config;

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
                .requestMatchers(HttpMethod.POST, "/api/v1/books", "/api/v1/books/").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/books/{id}").hasAuthority("ROLE_ADMIN") // delete book
                .requestMatchers(HttpMethod.PUT, "/api/v1/books/{id}").hasAuthority("ROLE_ADMIN") // update book
                .requestMatchers(HttpMethod.POST, "/api/v1/books/validate").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/books",
                        "/api/v1/books/",
                        "/api/v1/books/{id}").permitAll() // get books, book by id
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/categories",
                        "/api/v1/categories/",
                        "/api/v1/categories/paged",
                        "/api/v1/categories/{id}").permitAll() // get categories, category by id
                .anyRequest().authenticated());

        http.oauth2ResourceServer(c -> c.jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakAuthenticationConverter())));

        return http.build();
    }

}
