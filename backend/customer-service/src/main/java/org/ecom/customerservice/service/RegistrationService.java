package org.ecom.customerservice.service;

import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.core.Response;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecom.customerservice.config.KeycloakProperties;
import org.ecom.customerservice.dto.RegistrationRequest;
import org.ecom.customerservice.exception.EmailAlreadyExistsException;
import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.repository.CustomerRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(KeycloakProperties.class)
public class RegistrationService {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProps;
    private final CustomerRepository customerRepository;

    public void registerUser(RegistrationRequest request) {
        RealmResource realmResource = keycloak.realm(keycloakProps.realm());

        UsersResource users = realmResource.users();

        List<UserRepresentation> search = users.search(request.email(), true);
        if (!search.isEmpty()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.email());
        user.setFirstName(request.firstname());
        user.setLastName(request.lastname());
        user.setEmail(request.email());
        user.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());

        user.setCredentials(Collections.singletonList(credential));

        try (Response response = users.create(user)) {
            if (response.getStatus() == 201) {
                saveUser(request, response);
                return;
            }

            throw new RuntimeException();
        }
    }

    private void saveUser(RegistrationRequest request, Response response) {
        String location = response.getHeaderString("Location");
        String externalId = location.substring(location.lastIndexOf("/") + 1);
        Customer customer = Customer.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .externalId(externalId)
                .build();
        customerRepository.save(customer);
    }

}
