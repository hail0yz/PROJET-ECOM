package org.ecom.customerservice.service;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import static java.util.Collections.singletonList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecom.customerservice.config.KeycloakProperties;
import org.ecom.customerservice.dto.RegistrationRequest;
import org.ecom.customerservice.exception.CustomerRegistrationFailedException;
import org.ecom.customerservice.exception.EmailAlreadyExistsException;
import org.ecom.customerservice.model.Customer;
import org.ecom.customerservice.model.Preferences;
import org.ecom.customerservice.repository.CustomerRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(KeycloakProperties.class)
public class RegistrationService {

    private final Keycloak keycloak;
    private final KeycloakProperties keycloakProps;
    private final CustomerRepository customerRepository;
    private final static String KEYCLOAK_CUSTOMER_ROLE = "USER";

    public void registerUser(RegistrationRequest request) {
        RealmResource realmResource = keycloak.realm(keycloakProps.realm());

        UsersResource users = realmResource.users();

        List<UserRepresentation> search = users.search(request.email(), true);
        if (!search.isEmpty()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        UserRepresentation user = buildUserRepresentation(request);

        String keycloakUserId;
        try (Response response = users.create(user)) {
            if (response.getStatus() != 201) {
                log.error("Failed to save user in keycloak. Response={}", response);
                throw new CustomerRegistrationFailedException("Failed to save user in keycloak");
            }

            keycloakUserId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        }

        log.debug("Customer {} was successfully registered in Keycloak {}", request.email(), keycloakUserId);

        assignRolesToUser(realmResource, keycloakUserId, KEYCLOAK_CUSTOMER_ROLE);

        Customer customer = Customer.builder()
                .id(keycloakUserId)
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .externalId(keycloakUserId)
                .preferences(Preferences.builder()
                        .emailNotificationsEnabled(true)
                        .smsNotificationsEnabled(false)
                        .build())
                .build();
        customerRepository.save(customer);
    }

    private UserRepresentation buildUserRepresentation(RegistrationRequest request) {
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
        user.setCredentials(singletonList(credential));

        return user;
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

    private void assignRolesToUser(RealmResource realmResource, String keycloakUserId, String roleName) {
        RoleRepresentation roleRepresentation = realmResource.roles().get(roleName).toRepresentation();

        realmResource.users().get(keycloakUserId)
                .roles()
                .realmLevel()
                .add(singletonList(roleRepresentation));

        log.debug("Successfully assigned role {} to user {}", roleName, keycloakUserId);
    }

}
